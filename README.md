Repository for the course "Compiladores" (EIC0028) of the second semester of the third year of MIEIC in FEUP.

In this repository there is the practical assignment of this course in the lective year of 2016/2017.

The aim of the project was to make a program capable of transforming basic javascript code to java using a given AST.
The program needs internet access in order to connect to http://esprima.org/ and get the associated AST.
Below there's a thorough explanation (in Portuguese) of the program and its proper use.

Credits:
* [Mário Fernandes](https://github.com/MarioFernandes73)
* [Inês Gomes](https://github.com/inesgomes)
* [Catarina Ramos](https://github.com/catramos96)

**PROJECT TITLE:** JS2JAVA

**GROUP:** G18

NAME1: Catarina Ramos, NR1: 201406219, GRADE1: 20, CONTRIBUTION1: 33%

NAME2: Inês Gomes, NR2: 201405778, GRADE2: 20, CONTRIBUTION2: 34%

NAME3: Mário Fernandes, NR3: 201201705, GRADE3: 20, CONTRIBUTION3: 33%
 
**SUMMARY:** O trabalho aqui apresentado é uma ferramenta que recebe uma secção restrita do universo de JavaScript e transforma num pedaço de código de Java, 
alertando o utilizador para eventuais erros semânticos que estará a introduzir. Para tal o utilizador apenas terá de inserir o seu código JavaScript. 
A ferramenta acede ao site "Esprima" por meio de um web crawler, insere o código digitado e recebe uma AST em JSON do mesmo. 
  
Esta ferramenta implica que o código javascript esteja em funções para a contrução das Symbol Tables. A secção de código de javascript que pode ser testada inclui:

 * funções com argumentos e valores de retorno;
 * chamada de funções;
 * criação e atribuição de valores a variáveis;
 * expressões aritméticas;
 * condições com operadores lógicos;
 * ciclos (while, do while, for);
 * arrays de várias dimensões.
 
 
**EXECUTE:** Abrir um IDE, incluir as bibliotecas e correr gui.Interface. Depois é só digitar o código, e clicar "convert". Para obter os ficheiros da HIR, Symbol Table e do código Java clicar "Create Files". Mais informações em "Info".
 
**DEALING WITH SYNTACTIC ERRORS:** O facto de dependermos de uma terceira ferramenta para a conversão, que neste caso é o Esprima, implica usar o tratamento 
de erros sintáticos que o "Esprima" utilizada. Neste caso o "Esprima" não tolera erros sintáticos, pelo que, se o utilizador digitar um texto com erros, o 
"Esprima" devolve uma mensagem de erro que mostramos ao utilizador. A partir daqui, o programa fica à espera de um novo input, sem processar o anterior.
 
**SEMANTIC ANALYSIS:** Durante a interpretação da AST e inferência de tipos é possível encontrar diversos erros semânticos, como por exemplo:

 * variáveis não inicializadas;
 * variáveis inicializas com um tipo e que recebem outro;
 * operações entre tipos inválidas;
 * chamada de funções inexistentes;
 * chamada de funções com argumentos errados (numero e tipo);
 * tipo de retorno errado; 
 
**INTERMEDIATE REPRESENTATIONS (IRs):** Para este trabalho foi necessária a construção de uma HIR. Esta HIR apresenta uma estrutura semelhante à apresentada nas aulas teóricas. 
Para esta representação é usada a classe "Node", que tem um tipo (JSONType), especificação (e.g. se é um operador +,-,/,* ou o valor do literal), e uma referência para um descritor
da tabela de simbolos, caso haja essa relação. Um "Node" tem ainda um conjunto de nós adjacentes que serão os seus filhos. 
Existe um nó inicial, chamado START que tem como filhos diretos as FUNCTIONS. A partir daí, todos os nós terão um dos tipos do enum JSONType. Stores e loads são identificados através 
da especificação do nó. 
 
**CODE GENERATION:** Para a geração de código, a HIR gerada apartir do parser é percorrida e por cada nó é avaliado o seu JSONType. A geração de código começa com a chamada da função generate(Node node) onde o parâmetro node, inicialmente, é a root da HIR. Dentro desta função, o tipo de JSONType do nó é avaliado e encaminhado para um handler específico que retorna uma string com o código gerado. Dentro destes handlers, pode ser particularmente relevante analisar os seus nós filhos, caso o tenham, e chamar de forma recursiva a função generate(Node n) por cada nó filho respetivo. A geração de código, em suma, traduz-se na concatenação dos códigos em string gerados por cada handler invocado. Os handlers existentes estão agrupados pelos seguintes grupos: function related, declaration and assignment, variables and values, operations and expressions, conditions structures e loops.
 
**OVERVIEW:** O trabalho desenvolvido está dividido em 4 partes: 

  * obtenção da AST em JSON do código Javascript;
  * construção de uma HIR (high-level intermediate representation) e duma Symbol Table de descritores; 
  * inferência de tipos e verificações semânticas;
  * geração de código.
  
Para a 1ª parte, foi usada a biblioteca "Selenium" que consiste num WebDriver que permite aceder ao website da ferramenta "Esprima" e manipular a página de modo
a obter a AST gerada. 
Para a 2º parte, foi usada a biblioteca GSON para simplificar a leitura do JSON gerado pela AST. 
Tanto para a construção do parser como para a geração do código e inferência de tipos é usado backtracking para percorrer a àrvore.
 
**TESTSUITE AND TEST INFRASTRUCTURE:** Para a execução dos nossos testes é necessário correr a aplicação e copiar o conteúdo dos ficheiros .txt para a ferramenta
e clicar "Convert". Para analisar melhor os resultados é possível criar ficheiros .txt com os resultados da HIR, Symbol Table e do código Java que irão aparecer
na pasta "results".
 
**TASK DISTRIBUTION:**

Catarina Ramos:
Geração de código
Tratamento de alguns erros

Inês Gomes:
Interface e integração com webcrawler
Parser (HIR + Symbol Table)

Mário Fernandes:
Inferência de tipos
Tratamento de alguns erros

**PROS:**
A ferramenta é simples e intuitiva de usar devido à sua interface.
Existe a opção de poder analisar a HIR gerada correspondente ao código, os possíveis erros semânticos/sintáticos associados, a tabela de simbolos gerada incluíndo a inferência de tipos e ainda a geração de código na linguagem Java.
O utilizador ainda tem a possibilidade de poder guardar este código. 
 
**CONS:**
Esta ferramenta é limitada pois não cobre muitas das funcionalidades e métodos da linguagem de javascript.
Não está preparada para lidar com null nem com possíveis retornos ao invocar uma outra função. Também é muito restrita com function calls.
