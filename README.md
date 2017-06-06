**PROJECT TITLE:** JS2JAVA

**GROUP:** G18

NAME1: Catarina Ramos, NR1: 201406219, GRADE1: 20, CONTRIBUTION1: 33%

NAME2: Inês Gomes, NR2: 201405778, GRADE2: 20, CONTRIBUTION2: 34%

NAME3: Mário Fernandes, NR3: 201201705, GRADE3: 20, CONTRIBUTION3: 33%
 
**SUMMARY:** O trabalho aqui apresentado é uma ferramenta que recebe uma secção restrita do universo de JavaScript e transforma num pedaço de código de Java, alertando o utilizador para eventuais erros semânticos que estará a introduzir. Para tal o utilizador apenas terá de inserir o seu código JavaScript. A ferramenta acede ao site "Esprima" por meio de um web crawler, insere o código digitado e recebe uma AST em JSON do mesmo. O trabalho desenvolvido está dividido em 3 partes: 

  * construção de uma HIR (high-level intermediate representation) e duma Symbol Table de descritores; 
  * inferência de tipos e verificações semânticas;
  * geração de código.
  
Esta ferramenta implica que o código javascript esteja em funções para a contrução das Symbol Tables. A secção de código de javascript que pode ser testada inclui:

 * funções com argumentos e valores de retorno;
 * chamada de funções;
 * criação e atribuição de valores a variáveis;
 * expressões aritméticas;
 * condições com operadores lógicos;
 * ciclos (while, do while, for);
 * arrays de várias dimensões.
 
**EXECUTE:** (indicate how to run your tool)
 
**DEALING WITH SYNTACTIC ERRORS: ** O facto de dependermos de uma terceira ferramenta para a conversão, que neste caso é o Esprima, implica usar o tratamento de erros sintáticos que o "Esprima" utilizada. Neste caso o "Esprima" não tolera erros sintáticos, pelo que, se o utilizador digitar um texto com erros, o "Esprima" devolve uma mensagem de erro que mostramos ao utilizador. A partir daqui, o programa fica à espera de um novo input, sem processar o anterior.
 
**SEMANTIC ANALYSIS:** Durante a interpretação da AST e inferência de tipos é possível encontrar diversos erros semânticos, como por exemplo:

 * variáveis não inicializadas;
 * 
 
**INTERMEDIATE REPRESENTATIONS (IRs):** (for example, when applicable, briefly describe the HLIR (high-level IR) and the LLIR (low-level IR) used, if your tool includes an LLIR with structure different from the HLIR)
 
**CODE GENERATION:** (when applicable, describe how the code generation of your tool works and identify the possible problems your tool has regarding code generation.)
 
**OVERVIEW:** (refer the approach used in your tool, the main algorithms, the third-party tools and/or packages, etc.)
 
**TESTSUITE AND TEST INFRASTRUCTURE:** (Describe the content of your testsuite regarding the number of examples, the approach to automate the test, etc.)
 
**TASK DISTRIBUTION:** (Identify the set of tasks done by each member of the project.)
 
**PROS:** (Identify the most positive aspects of your tool)
 
**CONS:** (Identify the most negative aspects of your tool)
