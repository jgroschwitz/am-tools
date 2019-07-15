/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.saar.coli.amrtagging.formalisms.ucca.tools;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import de.saar.coli.amrtagging.AMDependencyTree;
import de.saar.coli.amrtagging.Alignment;
import de.saar.coli.amrtagging.ConcreteAlignmentTrackingAutomaton;
import de.saar.coli.amrtagging.ConllSentence;
import de.saar.coli.amrtagging.GraphvizUtils;
import de.saar.coli.amrtagging.MRInstance;
import de.saar.coli.amrtagging.SupertagDictionary;
import de.saar.coli.amrtagging.formalisms.ConcreteAlignmentSignatureBuilder;
import de.saar.coli.amrtagging.formalisms.ucca.UCCABlobUtils;
import de.up.ling.irtg.Interpretation;
import de.up.ling.irtg.InterpretedTreeAutomaton;
import de.up.ling.irtg.algebra.ParserException;
import de.up.ling.irtg.algebra.StringAlgebra;
import de.up.ling.irtg.algebra.graph.GraphAlgebra;
import de.up.ling.irtg.algebra.graph.SGraph;
import de.up.ling.irtg.algebra.graph.SGraphDrawer;
import de.up.ling.irtg.automata.ConcreteTreeAutomaton;
import de.up.ling.irtg.corpus.Corpus;
import de.up.ling.irtg.corpus.CorpusReadingException;
import de.up.ling.irtg.corpus.Instance;
import de.up.ling.irtg.hom.Homomorphism;
import de.up.ling.irtg.signature.Signature;
import de.up.ling.tree.ParseException;
import de.up.ling.tree.Tree;
import edu.stanford.nlp.simple.Sentence;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Create UCCA training data (AMConll).
 *
 * @author matthias, Mario
 */
public class CreateCorpus {
    @Parameter(names = {"--corpus", "-c"}, description = "Path to the input corpus ")//, required = true)
    private String corpusPath = "/home/matthias/Schreibtisch/Hiwi/Mario/test_decomposition.irtg";

    @Parameter(names = {"--outPath", "-o"}, description = "Path for output files")//, required = true)
    private String outPath = "/home/matthias/Schreibtisch/Hiwi/Mario/";

    @Parameter(names = {"--prefix", "-p"}, description = "Prefix for output file names (e.g. train --> train.amconll)")
//, required=true)
    private String prefix = "train";

    @Parameter(names = {"--vocab", "-v"}, description = "vocab file containing supertags (e.g. points to training vocab when doing dev/test files)")
    private String vocab = null;

    @Parameter(names = {"--debug"}, description = "Enables debug mode")
    private boolean debug = false;

    @Parameter(names = {"--help", "-?", "-h"}, description = "displays help if this is the only command", help = true)
    private boolean help = false;


    public static void main(String[] args) throws FileNotFoundException, IOException, ParseException, ParserException, AMDependencyTree.ConllParserException, CorpusReadingException {
        CreateCorpus cli = new CreateCorpus();
        JCommander commander = new JCommander(cli);

        try {
            commander.parse(args);
        } catch (com.beust.jcommander.ParameterException ex) {
            System.err.println("An error occured: " + ex.toString());
            System.err.println("\n Available options: ");
            commander.usage();
            return;
        }

        if (cli.help) {
            commander.usage();
            return;
        }

        InterpretedTreeAutomaton loaderIRTG = new InterpretedTreeAutomaton(new ConcreteTreeAutomaton());
        Signature dummySig = new Signature();
        loaderIRTG.addInterpretation("id", new Interpretation(new StringAlgebra(), new Homomorphism(dummySig, dummySig)));
        loaderIRTG.addInterpretation("string", new Interpretation(new StringAlgebra(), new Homomorphism(dummySig, dummySig)));
        loaderIRTG.addInterpretation("graph", new Interpretation(new GraphAlgebra(), new Homomorphism(dummySig, dummySig)));
        loaderIRTG.addInterpretation("alignment", new Interpretation(new StringAlgebra(), new Homomorphism(dummySig, dummySig)));

        Corpus corpus = Corpus.readCorpusWithStrictFormatting(new FileReader(cli.corpusPath), loaderIRTG);

        int counter = 0;
        int problems = 0;
        ArrayList<ConllSentence> outCorpus = new ArrayList<>();
        SupertagDictionary supertagDictionary = new SupertagDictionary();


        if (cli.vocab != null) { //vocab given, read from file
            supertagDictionary.readFromFile(cli.vocab);
        }
        for (Instance corpusInstance : corpus) {
            if (counter % 10 == 0 && counter > 0) {
                System.err.println(counter);
                System.err.println("decomposable so far " + 100 * (1.0 - (problems / (float) counter)) + "%");
            }
            if (counter % 1000 == 0 && counter > 0) { //every now and then write intermediate results.
                cli.write(outCorpus, supertagDictionary);
            }
            counter++;
            //read graph, string and alignment from corpus
            SGraph graph = (SGraph) corpusInstance.getInputObjects().get("graph");
            List<String> sentence = (List) corpusInstance.getInputObjects().get("string");
            List<String> als = (List) corpusInstance.getInputObjects().get("alignment");
            if (als.size() == 1 && als.get(0).equals("")) {
                //System.err.println("Repaired empty alignment!");
                als = new ArrayList<>();
            }
            String[] alStrings = als.toArray(new String[0]);
            ArrayList<Alignment> alignments = new ArrayList<>();
            for (String alString : alStrings) {
                Alignment al = Alignment.read(alString, 0);
                alignments.add(al);
            }
            //create MRInstance object that bundles the three:
            MRInstance inst = new MRInstance(sentence, graph, alignments);
            System.out.println(GraphvizUtils.simpleAlignViz(inst, true)); //this generates a string that you can compile with graphviz dot to get a visualization of what the grouping induced by the alignment looks like.
            //System.out.println(inst.getSentence());
            //System.out.println(inst.getAlignments());
            //System.out.println(inst.getGraph().);
            //SGraphDrawer.draw(inst.getGraph(), ""); //display graph
            //break;
            ConcreteAlignmentSignatureBuilder sigBuilder = new ConcreteAlignmentSignatureBuilder(inst.getGraph(), inst.getAlignments(), new UCCABlobUtils());
            try {
                ConcreteAlignmentTrackingAutomaton auto = ConcreteAlignmentTrackingAutomaton.create(inst, sigBuilder, false);
                auto.processAllRulesBottomUp(null);
                Tree<String> t = auto.viterbi();

                if (t != null) { //graph can be decomposed
                    //SGraphDrawer.draw(inst.getGraph(), ""); //display graph
                    ConllSentence sent = ConllSentence.fromIndexedAMTerm(t, inst, supertagDictionary);

                    Sentence stanfAn = new Sentence(inst.getSentence()); //remove artifical root "word"

                    sent.addPos(stanfAn.posTags());

                    //sent.addNEs(stanfAn.nerTags()); //slow, only add this for final creation of training data

                    sent.addLemmas(stanfAn.lemmas());

                    outCorpus.add(sent); //done with this sentence
                    //we can also create an AM dependency tree now
                    AMDependencyTree amdep = AMDependencyTree.fromSentence(sent);
                    //use one of these to get visualizations
                    //amdep.getTree().map(ent -> ent.getForm() + " " + ent.getDelexSupertag() + " " + ent.getType().toString() +" "+ent.getEdgeLabel()).draw();
                    //amdep.getTree().map(ent -> ent.getForm() +" "+ent.getEdgeLabel()).draw();

                    //this is how we can get back the graph (with alignment to positions where the individual parts came from):
                    //SGraph alignedGraph = amdep.evaluate(true);
                    //SGraphDrawer.draw(alignedGraph, "Reconstructed Graph");

                } else {
                    //TODO: implement something so that you can easily look at problems here.
                    problems++;
                    System.err.println("not decomposable " + inst.getSentence());
                    if (cli.debug) {
                        //check if we can get a graph constant for every alignment
                        //if we cannot get at least one graph constant for any blob, the decomposition has no choice but fail.
                        // Addition by JG: also, the constants can reaveal other causes for a failed decomposition, such as 
                        for (Alignment al : inst.getAlignments()) {
                            System.err.println(inst.getSentence().get(al.span.start));
                            System.err.println(sigBuilder.getConstantsForAlignment(al, inst.getGraph(), false));
                        }
                        System.err.println(GraphvizUtils.simpleAlignViz(inst));

                        //add the next lines to get the graph printed / drawn
                        //System.err.println(inst.getGraph().toIsiAmrStringWithSources());
                        //SGraphDrawer.draw(inst.getGraph(), "");
                    }

                    if (problems > 1) { //ignore the first problems
                        //SGraphDrawer.draw(inst.getGraph(), "");
                        //break;
                    }
                }
            } catch (Exception ex) {
                problems++;
                System.err.println("Ignoring an exception:");
                ex.printStackTrace();
                for (Alignment al : inst.getAlignments()) {
                    System.err.println(inst.getSentence().get(al.span.start));

                    try {
                        System.err.println(sigBuilder.getConstantsForAlignment(al, inst.getGraph(), false));
                    } catch (Exception e) {
                        System.err.printf("Additionally, could not print constantsForAlignment: %s\n", e);
                    }
                }
            }
        }
        System.err.println("ok: " + (counter - problems));
        System.err.println("total: " + counter);
        System.err.println("i.e. " + 100 * (1.0 - (problems / (float) counter)) + "%");
        cli.write(outCorpus, supertagDictionary);

    }


    private void write(ArrayList<ConllSentence> outCorpus, SupertagDictionary supertagDictionary) throws IOException {
        if (outPath != null && prefix != null) {
            ConllSentence.writeToFile(outPath + "/" + prefix + ".amconll", outCorpus);
            if (vocab == null) { //only write vocab if it wasn't restored.
                supertagDictionary.writeToFile(outPath + "/" + prefix + "-supertags.txt");
            }
        }
    }


}
    

    

