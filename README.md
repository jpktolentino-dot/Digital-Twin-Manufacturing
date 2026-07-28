# Digital Twin Manufacturing

Plataforma web de **Gêmeo Digital (Digital Twin)** para representação virtual de uma planta industrial, desenvolvida com base nos princípios da Indústria 4.0 e da manufatura inteligente.

## Sobre o Projeto

Organizações industriais frequentemente têm suas informações de produção, funcionamento de máquinas e indicadores de desempenho distribuídas entre diferentes sistemas, dificultando a supervisão integrada da operação, a identificação de gargalos produtivos e a tomada de decisões em tempo real.

Este projeto propõe uma solução que reproduz digitalmente o comportamento de uma fábrica, integrando conceitos de **IoT**, **computação em nuvem** e **análise de dados** em uma interface única, permitindo acompanhar o estado operacional dos equipamentos, o fluxo de produção e os principais indicadores industriais.

## Objetivo Geral

Desenvolver uma plataforma web que implemente um Digital Twin de uma planta industrial, representando virtualmente o estado operacional da fábrica e disponibilizando informações atualizadas em tempo real sobre produção, equipamentos e indicadores de desempenho.

## Funcionalidades Principais

### Cadastro e Gerenciamento
- Setores produtivos
- Máquinas (vinculadas a setores, com informações técnicas e operacionais)
- Operadores
- Produtos
- Ordens de produção (produtos, recursos, responsáveis e estágio atual)

### Monitoramento
Acompanhamento contínuo das condições operacionais de cada equipamento, incluindo, no mínimo:
- Estado atual (operando, parada, manutenção ou falha)
- Tempo de funcionamento e tempo de parada
- Consumo energético
- Quantidade de itens produzidos
- Histórico de dados para análises e comparações entre períodos

### Indicadores de Desempenho (KPIs)
- **OEE (Overall Equipment Effectiveness)** — calculado a partir de:
  - Disponibilidade
  - Performance
  - Qualidade
- Tempo de parada
- Produtividade da linha de produção
- Eficiência operacional
- Consumo energético
- Volume diário de produção

### Painel Gerencial (Dashboard)
- Mapa da planta industrial com estado operacional das máquinas
- Fluxo de produção entre setores
- KPIs e gráficos históricos
- Painéis com métricas consolidadas de produção diária, eficiência, disponibilidade e utilização de recursos
- Interface voltada à rápida identificação de falhas e gargalos

### Registro de Eventos
Registro de ocorrências relevantes da operação, contendo:
- Data e horário
- Equipamento envolvido
- Categoria do evento (interrupção, falha, conclusão de ordem, manutenção, retomada etc.)
- Descrição da ocorrência
- Impacto estimado na produção

### Simulação da Planta
Mecanismo de simulação para reproduzir o comportamento dinâmico da fábrica, permitindo que as máquinas alternem automaticamente entre estados operacionais (ciclos de produção, inatividade, manutenções programadas e falhas), viabilizando testes da plataforma sem equipamentos físicos conectados.

## Diferenciais (Opcionais)

- Animações representando o funcionamento das máquinas e da linha de produção
- Simulação de diferentes cenários de produção
- Comparação entre períodos de operação
- Geração automática de relatórios gerenciais em PDF
- Análise histórica avançada de indicadores

## Funcionalidades Avançadas (Extras)

- Previsão de falhas baseada em séries temporais
- Manutenção preditiva
- Integração com sensores IoT simulados
- Modelos de inteligência artificial para estimativa de produtividade

## Arquitetura

O sistema deverá ser estruturado em **arquitetura multicamadas**, integrado a banco de dados, contemplando:

- Camada de apresentação (dashboard/interface web)
- Camada de aplicação/regras de negócio
- Camada de dados (persistência)
- Módulo de simulação da planta

## Entregáveis

- [ ] Aplicação web plenamente funcional
- [ ] Documentação técnica
- [ ] Modelo de dados
- [ ] Descrição da arquitetura do sistema
- [ ] Manual de instalação e execução
- [ ] Evidências de testes realizados
- [ ] Apresentação demonstrando o funcionamento completo do Digital Twin em cenário simulado

## Tecnologias

> *A definir pela equipe de desenvolvimento.*

Sugestões de stack (a confirmar):
- **Frontend:** Framework web para dashboard interativo (ex.: React, Vue, Angular)
- **Backend:** API REST (ex.: Node.js, Python/Django/FastAPI, Java/Spring)
- **Banco de Dados:** Relacional e/ou séries temporais (ex.: PostgreSQL, InfluxDB)
- **Simulação:** Serviço/worker responsável pela geração de dados simulados dos equipamentos
- **Visualização:** Biblioteca de gráficos (ex.: Chart.js, D3.js, Recharts)

## Instalação e Execução

> *Seção a ser preenchida conforme a stack definida pela equipe.*

```bash
# Clonar o repositório
git clone <url-do-repositorio>

# Instalar dependências
# (instruções específicas do backend e frontend)

# Configurar variáveis de ambiente
# (ex.: conexão com banco de dados)

# Executar a aplicação
```

## Equipe

| Nome                         | Função |
| João Pedro Kloster Tolentino | Gerente do Projeto |
| Ramon Albini                 | Back-End |
| Eduardo Henrique Rodrigues   | Front-End |
| Murilo Arruda                | Testes |
| Murilo Meister               | Documentação |


## Licença
Projeto acadêmico/fictício — inspirado nos princípios da Indústria 4.0 e manufatura inteligente.
Projeto acadêmico/fictício — inspirado nos princípios da Indústria 4.0 e manufatura inteligente.

