package de.saar.coli.amtools.script.amr_templates;

import de.saar.basic.Pair;
import de.up.ling.irtg.Interpretation;
import de.up.ling.irtg.InterpretedTreeAutomaton;
import de.up.ling.irtg.algebra.graph.ApplyModifyGraphAlgebra;
import de.up.ling.irtg.algebra.graph.SGraph;
import de.up.ling.irtg.util.Counter;
import de.up.ling.tree.ParseException;
import de.up.ling.tree.Tree;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class SampleFromTemplate {

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void main(String[] args) throws IOException, ParseException {
        int numSamples = 25;

        String output_file = "examples/amr_template_grammars/pp_attachment_to.txt";
        String description = "Prepositional Phrase attachment ambiguities. Created by a grammar.";

//        InterpretedTreeAutomaton irtg = InterpretedTreeAutomaton.fromPath("examples/amr_template_grammars/unisex_names.irtg");
        InterpretedTreeAutomaton irtg = InterpretedTreeAutomaton.fromPath("examples/amr_template_grammars/alternative_to.irtg");


        Interpretation stringInterp = irtg.getInterpretation("string");
        Interpretation graphInterp = irtg.getInterpretation("graph");
        Map<String, List<Tree<String>>> templateCounter = new HashMap<>();
        for (Tree<String> grammarTree : irtg.getAutomaton().language()) {
            templateCounter.computeIfAbsent(grammarTree.getLabel(), s -> new ArrayList<>()).add(grammarTree);
        }
        List<Tree<String>> samples = new ArrayList<>();
        for (String template : templateCounter.keySet()) {
            System.out.println(template);
            System.out.println(templateCounter.get(template).size());
            List<Tree<String>> trees = templateCounter.get(template);
            Collections.shuffle(trees);
            for (int i = 0; i < Math.min(numSamples, trees.size()); i++) {
                Tree<String> tree = trees.get(i);
                samples.add(tree);
                Object stringResult = stringInterp.getAlgebra().evaluate(stringInterp.getHomomorphism().apply(tree));
                Object graphResult = graphInterp.getAlgebra().evaluate(graphInterp.getHomomorphism().apply(tree));
                String sentenceString = stringInterp.getAlgebra().representAsString(stringResult);
                System.out.println(sentenceString);
                String graphString = fixAMRString(((Pair<SGraph, ApplyModifyGraphAlgebra.Type>)graphResult).left.toIsiAmrString());
                System.out.println(graphString);
            }
            System.out.println("\n\n\n");
        }

        writeSamplesToFile(output_file, samples, description, irtg);
    }


    public static String fixAMRString(String amrString) {
        amrString = amrString.replaceAll("(:op[0-9]+) ([^ ()]+)", "$1 \"$2\"");
        amrString = amrString.replaceAll("\"\\+\"", "+");
        return amrString.replaceAll("(:wiki) ([^ ()]+)", "$1 \"$2\"");
    }


    public static void writeSamplesToFile(String fileName, Iterable<Tree<String>> samples, String description, InterpretedTreeAutomaton irtg) throws IOException {
        FileWriter w = new FileWriter(fileName);
        w.write("# " + description+"\n\n");
        Interpretation stringInterp = irtg.getInterpretation("string");
        Interpretation graphInterp = irtg.getInterpretation("graph");
        for (Tree<String> sample : samples) {
            Object stringResult = stringInterp.getAlgebra().evaluate(stringInterp.getHomomorphism().apply(sample));
            Object graphResult = graphInterp.getAlgebra().evaluate(graphInterp.getHomomorphism().apply(sample));
            String sentenceString = postprocessString((List<String>)stringResult);
            w.write("# ::snt " + sentenceString+"\n");
            String graphString = fixAMRString(((Pair<SGraph, ApplyModifyGraphAlgebra.Type>)graphResult).left.toIsiAmrString());
            w.write(graphString+"\n\n");
        }
        w.close();
    }

    public static String postprocessString(List<String> tokens) {
        return tokens.stream().collect(Collectors.joining(" ")).replaceAll(" , ", ", ")
                .replaceAll(" \\.", ".");
    }
}