# Editor de Desenho Vetorial Interativo

Projeto acadêmico desenvolvido em **Java** com **JavaFX** e **Maven** para a disciplina de Programação Orientada a Objetos 1 (POO 1). O aplicativo permite desenhar e manipular formas geométricas em uma área de desenho, organizar objetos em grupos, usar uma grade e salvar/carregar projetos em JSON.
Realizado com auxílio da 

## Funcionalidades

- Desenho de polígonos por cliques no Canvas.
- Criação de círculos, quadrados e hexágonos por arraste do mouse.
- Seleção, movimentação e seleção múltipla de formas.
- Edição de vértices de polígonos.
- Transformações geométricas: escala, rotação e cisalhamento.
- Agrupamento e desagrupamento de formas.
- Organização da ordem das formas, trazendo para frente ou enviando para trás.
- Duplicação das formas selecionadas.
- Ajuste de coordenadas com **Snap to Grid**.
- Barra lateral para ferramentas e propriedades visuais.
- Painel de status com coordenadas e informações das formas.
- Histórico de desfazer/refazer, acessado por atalhos de teclado.
- Salvamento, abertura e salvamento com outro nome em formato JSON.
- Validação de arquivos e mensagens de erro para problemas de leitura, gravação ou conteúdo inválido.
- Aviso antes de fechar o aplicativo ou substituir o desenho quando há alterações não salvas.

## Tecnologias

- **Java 17 ou superior**
- **JavaFX 17 ou superior**
- **Apache Maven** para gerenciamento e execução do projeto
- **Gson 2.14.0** para serialização e leitura de JSON

## Pré-requisitos

Antes de executar o projeto, instale:

1. JDK 17 ou superior.
2. IntelliJ IDEA (ou outra IDE compatível com Java e Maven).
3. Maven configurado, caso deseje executar o projeto pelo terminal.

O arquivo `pom.xml` deve conter as dependências do JavaFX e a dependência Gson abaixo dentro da seção `<dependencies>`:

```xml
<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
    <version>2.14.0</version>
</dependency>
```

Não substitua as demais dependências existentes no `pom.xml`. Depois de alterar o arquivo, recarregue o projeto Maven na IDE.

## Como abrir e executar no IntelliJ IDEA

1. Baixe ou clone o repositório Git.
2. Abra no IntelliJ IDEA a pasta que contém o `pom.xml`.
3. Aguarde a importação do projeto Maven e o download das dependências. Se necessário, abra a janela **Maven** e use **Reload All Maven Projects**.
4. Localize `MainApp.java` em `src/main/java/org/example/`.
5. Execute `MainApp.java` usando a configuração de execução do IntelliJ.

Se o projeto estiver configurado com o plugin JavaFX do Maven, também é possível executar pelo terminal, na pasta do projeto:

```bash
mvn clean javafx:run
```

Se o comando `mvn` não for reconhecido, verifique se o Maven está instalado e configurado no `PATH`, ou execute o projeto pela IDE.

## Atalhos do teclado

| Tecla/atalho | Ação |
|---|---|
| `D` | Ativar desenho de polígonos |
| `S` | Ativar seleção e movimentação |
| `C` | Ativar criação de círculo |
| `Q` | Ativar criação de quadrado |
| `H` | Ativar criação de hexágono |
| `E` | Ativar escala |
| `R` | Ativar rotação |
| `T` | Ativar cisalhamento |
| `V` | Ativar edição de vértices |
| `G` | Agrupar as formas selecionadas |
| `U` | Desagrupar formas selecionadas |
| `F` | Trazer forma(s) selecionada(s) para frente |
| `B` | Enviar forma(s) selecionada(s) para trás |
| `N` | Ativar/desativar Snap to Grid |
| `Ctrl + D` | Duplicar formas selecionadas |
| `Ctrl + Z` | Desfazer |
| `Ctrl + Y` | Refazer |

### Desenhar formas

- **Polígono:** pressione `D`, clique para marcar os vértices e dê duplo clique para finalizar. O polígono precisa ter pelo menos três vértices.
- **Círculo:** pressione `C` e arraste o mouse para definir a forma.
- **Quadrado:** pressione `Q` e arraste o mouse para definir a forma.
- **Hexágono:** pressione `H` e arraste o mouse para definir a forma.
- **Selecionar:** pressione `S` e clique sobre uma forma. Para selecionar uma área, arraste o mouse no Canvas conforme o comportamento da ferramenta de seleção.

As ferramentas de transformação e edição são ativadas pelos atalhos correspondentes e operam de acordo com a interação implementada na ferramenta ativa.

## Salvar e abrir projetos

Use o menu **Arquivo** da aplicação:

- **Salvar:** grava o projeto no arquivo atual. Na primeira gravação, permite escolher um local e nome.
- **Salvar como...:** permite escolher outro nome ou local.
- **Abrir JSON...:** abre um projeto salvo anteriormente.

Os arquivos são gravados em JSON e incluem as dimensões da área de desenho, a configuração do Snap to Grid e as formas, com seus pontos/coordenadas, tipo, cores, espessura de borda e identificador de grupo, conforme aplicável.

O carregamento valida o conteúdo antes de substituir o desenho que já está aberto. Se o arquivo estiver corrompido, incompleto ou inválido, a aplicação mostra um alerta e preserva o desenho atual. Problemas de leitura e gravação também são apresentados em mensagens para o usuário.

O aplicativo pergunta antes de fechar ou abrir outro projeto quando detecta alterações não salvas. Nessa situação, é possível salvar, descartar as alterações ou cancelar a operação.

### Observação sobre círculos cisalhados

O modelo atual não restaura a flag interna de cisalhamento do círculo. Quando um círculo cisalhado é salvo e reaberto, sua geometria é preservada como um polígono com o mesmo contorno, mas deixa de ser representada internamente como um objeto `Circulo`.

## Organização do código

O código-fonte segue a separação em pacotes `Models`, `Controller` e `Views`.

```text
src/
└── main/
    └── java/
        └── org/
            └── example/
                ├── MainApp.java
                ├── Models/
                │   ├── FormaGeometrica.java
                │   ├── FormaBase.java
                │   ├── Ponto.java
                │   ├── Poligono.java
                │   ├── Circulo.java
                │   ├── Quadrado.java
                │   └── Hexagono.java
                ├── Controller/
                │   ├── EditorContext.java
                │   ├── HistoricoController.java
                │   ├── Ferramenta.java
                │   └── Ferramenta*.java
                └── Views/
                    ├── ContainerApp.java
                    ├── BarraLateralView.java
                    ├── PainelStatusView.java
                    └── PersistenciaJson.java
```

A lista representa a organização principal esperada do projeto; os nomes e a quantidade exata de classes de ferramentas podem variar conforme a versão do repositório.

### Responsabilidades principais

- **Models:** representam os pontos e as formas geométricas e implementam cálculos e transformações.
- **Controller:** gerencia o contexto do editor, as ferramentas, a seleção, os agrupamentos, as operações de camadas e o histórico.
- **Views:** implementa o Canvas, a barra lateral, o painel de status e a interação com o usuário. `PersistenciaJson` trata a serialização e desserialização dos projetos.

## Padrões e conceitos de POO

O projeto foi estruturado para praticar os seguintes conceitos e padrões:

- **State:** a ferramenta ativa define o comportamento da interação com o mouse.
- **Prototype:** a duplicação de formas utiliza a operação de clonagem do modelo.
- **Memento/Undo-Redo:** o histórico mantém estados anteriores para permitir desfazer e refazer operações.
- **Composite por identificador de grupo:** formas relacionadas compartilham um `groupId` e podem ser tratadas como um grupo em operações compatíveis.
- **Separação em camadas:** os modelos geométricos, os controles e a interface estão distribuídos em pacotes distintos.

## Validações e tratamento de erros

A persistência verifica a estrutura do JSON, a versão do documento, as dimensões do Canvas, os tipos de formas, as coordenadas, as cores, a espessura das bordas e a quantidade de objetos/pontos. Arquivos inválidos ou incompatíveis produzem alertas explicativos em vez de substituir silenciosamente o desenho atual.

## Possíveis melhorias futuras

- Ampliar os testes automatizados das ferramentas geométricas e do histórico.
- Restaurar círculos cisalhados como instâncias de `Circulo`, preservando também seu estado interno.
- Acrescentar testes específicos de arquivos JSON válidos, incompletos e corrompidos.
- Revisar a documentação de uso e atualizar as capturas de tela conforme a versão final da interface.

## Autor

Projeto acadêmico desenvolvido para a disciplina de Programação Orientada a Objetos 1.

## Uso de Inteligência Artificial

Durante o desenvolvimento deste projeto, utilizei o ChatGPT (OpenAI) como ferramenta de apoio e colaboração. A inteligência artificial contribuiu para a elaboração e modificação de partes do código-fonte, incluindo funcionalidades do editor, integração de componentes da interface gráfica, implementação do salvamento e carregamento de arquivos JSON e tratamento de erros.

Também utilizei a ferramenta para esclarecer dúvidas sobre Java e JavaFX, auxiliar na identificação e correção de erros e elaborar a documentação do projeto, incluindo este README.

As sugestões e os trechos de código gerados foram integrados ao projeto e testados no ambiente de desenvolvimento. O uso da inteligência artificial fez parte do processo de aprendizagem e construção da aplicação.
