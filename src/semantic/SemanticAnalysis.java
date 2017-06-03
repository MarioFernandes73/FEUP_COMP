package semantic;

import parser.Node;
import parser.SymbolTable;

import java.util.ArrayList;

public class SemanticAnalysis
{
    private ArrayList<SymbolTable> tables = new ArrayList<>();
    private Node hir;

    public SemanticAnalysis(ArrayList<SymbolTable> tables,Node hir){
        this.hir = hir;
        this.tables = tables;
    }

    public void run(){

    }

    /**
     *
     *
     * Calls to functions have the correct number of arguments,
     * the correct types of the arguments,
     * and the correct type for the return
     * ?
     */
}
