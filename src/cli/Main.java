package cli;

/**
 * Copyright 2017 SPeCS.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License. under the License.
 */

import parser.Descriptor;
import parser.Node;
import parser.Parser;
import parser.SymbolTable;
import semantic.TypeInference;

import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {

    	//o ficheiro sera recebido como parametro
    	Parser p = new Parser("test.json");
    	p.run();

        TypeInference ti = new TypeInference(p.getTables(),p.getHir());
        ti.run();

        /*CodeGenerator cg = new CodeGenerator(p.getHir(),p.getTables());
        cg.run();
        System.out.println(cg.getCode());*/

    	//TODO colocar isto num ficheiro
        System.out.println("\n------- START PRINTING HIR -------");
        printHIR(p.getHir(),"");
        System.out.println("\n------- START PRINTING SYMBOL TABLES -------");
        printSymbolTable(p.getTables());
    }

    private static void printHIR(Node n, String spacement)
    {
        System.out.println();
        System.out.println(spacement + "Type  : " + n.getType().toString());
        System.out.println(spacement + "Specification : "+n.getSpecification());

        Descriptor d = n.getReference();
        if(d != null)
            System.out.println(spacement + "Descriptor ( Name : "+ d.getName() + " | Type : "+ d.getType() +" )");

        for(Node n1 : n.getAdj())
        {
            printHIR(n1, spacement+"- ");
        }
    }

    private static void printSymbolTable(ArrayList<SymbolTable> tables)
    {
        System.out.println();

        for(SymbolTable st : tables)
        {
            System.out.println("Function \n   Name : " + st.getFunctionName() + "\n   Params : ");

            for(Descriptor d : st.params)
                System.out.println("      Name : " + d.getName() + "   AND   Type : " + d.getType());

            System.out.println("   Locals : ");
            for(Descriptor d : st.locals)
                System.out.println("      Name : " + d.getName() + "   AND   Type : " + d.getType());

            if(st.getFunctionReturn() != null)
                System.out.println("   Return Type : "+ st.getFunctionReturn().name());
            else
                System.out.println("   Return : void\n");
        }
    }
}
