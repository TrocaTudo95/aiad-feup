# AIAD

## Atribuição de meios do INEM a situações de emergência médica.

### 1. Sumário do problema que foi abordado na 1ª parte do projeto.

O Instituto Nacional de Emergência Médica, (INEM) é o Organismo do Ministério da Saúde responsável por coordenar o funcionamento, no território de Portugal Continental, de um Sistema Integrado de Emergência Médica (SIEM), de forma a garantir aos sinistrados ou vítimas de doença súbita a pronta e correcta prestação de cuidados de saúde.

Desta forma, é necessário realizar a melhor alocação de meios face às ocorrências que vão surgindo. 

Cada chamada efetuada para o CODU (centro de orientação de doentes urgentes) representa uma situação com diferente número de vítimas envolvidas, com diferentes gravidades afetando a prioridade da situação, diferentes distâncias.A alocação de recursos a cada situação deve ser dinâmica através da negociação entre as diferentes situações de emergência. Os agentes são por um lado as diferentes situações que necessitam de recursos e também os próprios recursos disponíveis

### 2. Definição do problema de análise de dados preditiva a resolver, enquadrado no âmbito do problema anterior.

Nesta segunda fase do projeto iremos utilizar o software RapidMiner. Através desta plataforma é possível realizar estudos de *machine learning, deep learning, text mining, e análise preditiva.* Será neste último ponto que focaremos a nossa atenção. Pretende-se obter um modelo que analise os dados recolhidos pelo nosso programa e consiga responder à seguinte questão: **Como varia o tempo de resposta a uma situação de emergência? **

Esta questão pretende auxiliar na tomada de decisão relativamente à aquisição de novos recursos (ambulâncias).

### 3. Variável dependente.

A variável dependente é o **tempo de resposta a uma situação de emergência**. Através desta medição será possível tirar conclusões acerca da eficiência do nosso sistema. Além de que, numa fase futura, conseguimos inferir como devemos alocar novos meios ao INEM, assim como as suas especificações (nomeadamente velocidade).

### 4. Alguns exemplos de variáveis independentes que pretendem implementar.

Deste modo, pretendemos realizar diversas experiências com geração aleatória de emergências onde iremos variar os seguintes parâmetros, sendo esses mesmos parâmetros as variáveis independentes:
- Velocidade de cada ambulância
- Número de ambulâncias
- Gravidade da emergência
- Número de emergências

