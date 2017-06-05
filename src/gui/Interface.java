package gui;

import cli.WebCrawler;
import codegeneration.CodeGenerator;
import parser.Parser;
import semantic.TypeInference;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.io.IOException;
import java.io.PrintWriter;

public class Interface
{
    private JPanel             panel1;
    private        JLabel      title;
    private        JLabel      JSCode;
    private        JTextArea   textAreaJSCode;
    private        JLabel      Console;
    private        JTabbedPane consoleOptions;
    private        JButton     ok;
    private        JButton     files;
    private        JTextArea   errorsText;
    private        JTextArea   javaCodeText;
    private        JTextArea   hirText;
    private        JTextArea   stText;
    private JButton            infoButton;

    private WebCrawler    wc;
    private Parser        p;
    private TypeInference ti;
    private CodeGenerator cg;

    public static void main(final String[] args)
    {
        JFrame frame = new JFrame("Interface");
        frame.setContentPane(new Interface().panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public Interface() { ok.addMouseListener(new MouseAdapter() {});
        ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                String errorMessages;

                wc = new WebCrawler(textAreaJSCode.getText());
                wc.run();
                errorMessages = wc.getErrorMessage();
                if(errorMessages != null)
                {
                    errorsText.setText("Semantic error :\n"+errorMessages);
                    return;
                }

                p = new Parser(wc.getJsonCode());
                p.run();
                errorMessages = p.getErrorMessage();
                if(errorMessages != null)
                {
                    errorsText.setText("Semantic error :\n"+errorMessages);
                    return;
                }

                ti = new TypeInference(p.getTables(),p.getHir());
                ti.run();
                errorMessages = ti.getErrorMessage();
                if(errorMessages != null)
                {
                    errorsText.setText("Semantic error :\n\n"+errorMessages);
                    return;
                }

                cg = new CodeGenerator(p.getHir(),p.getTables());
                //cg.run();

                errorsText.setText("Success!");
                javaCodeText.setText(cg.getCode());
                hirText.setText(cg.printHIR(p.getHir(),""));
                stText.setText(cg.printSymbolTable(p.getTables()));
            }
        });
        files.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                //writeInFile("output.java",cg.getCode());
                writeInFile("hir.txt",cg.printHIR(p.getHir(),""));
                writeInFile("symbolTable.txt",cg.printSymbolTable(p.getTables()));
            }

            private void writeInFile(final String filename, final String code)
            {
                try{
                    PrintWriter writer = new PrintWriter("result/"+filename, "UTF-8");
                    writer.println(code);
                    writer.close();
                } catch (IOException e) {
                    System.err.println("Error writing files");
                }
            }
        });
        infoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                JFrame frame = new JFrame("Info");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                JTextPane info = new JTextPane();
                info.setText("O que é ?\n\n"
                               + "Conversor de código Javascript para código Java, que usa a ferramenta \"Esprima\" para a conversão para AST. \n"
                               + "Esta AST é interpreta, criando uma tabela de símbolos e uma Representação de alto nível (HIR), onde é realizada a inferência de tipos e verificação de erros semânticos. \n"
                               + "Por fim é gerado o código Java com base nestas estruturas.\n\n\n"
                               + "O que é possível fazer ?\n\n"
                               + "  * declaração de funções com argumentos e retorno;\n"
                               + "  * chamada de funções;\n"
                               + "  * declaração de variaveis;\n"
                               + "  * operações aritméticas;\n"
                               + "  * ciclos e condições;\n"
                               + " \n\n"
                               + "Hint (limitações do JavaScript) : \n\n"
                               + "Aquando a chamada de funções, se a função tiver argumentos, chame numa primeira função e declare a função a seguir. \n"
                               + "Em caso de retorno de uma variável, declare primeiro a função e chame-a na função seguinte. \n"
                               + "Em caso de retorno de uma variável, a variável não deve ter argumentos.\n\n\n"
                               + "Trabalho realizado por : \n\n\n"
                               + "Catarina Ramos, Inês Gomes, Mário Fernandes\nCompiladores\n2016/2017\n\n\n");
                info.setFont(new Font("Arial", Font.LAYOUT_LEFT_TO_RIGHT, 12));
                frame.getContentPane().add(info, BorderLayout.CENTER);

                frame.pack();
                frame.setVisible(true);
            }
        });
    }

    private static void createUIComponents()
    {


    }
}
