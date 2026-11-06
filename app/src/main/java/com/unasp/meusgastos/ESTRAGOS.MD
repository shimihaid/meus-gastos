ESTRAGOS.md — Relatório dos Testes de Quebra (Etapa 7)
Este documento registra a análise técnica e comportamental dos 5 estragos propostos no tutorial para demonstrar os pilares da arquitetura MVVM.
Estrago 1: Instanciar o ViewModel com o construtor direto no lugar do viewModel()
Código alterado: vm: GastosViewModel = GastosViewModel() no parâmetro da @Composable fun TelaGastos.
•
Previsão: Ao re-renderizar a tela (recomposição causada por qualquer mudança de estado, como digitar no campo de texto), uma nova instância do GastosViewModel será criada do zero, perdendo o estado mantido pelo ViewModel.
•
O que aconteceu: Adicionaram-se dois gastos. Ao digitar qualquer letra no campo de descrição, a tela recompôs, o construtor GastosViewModel() foi chamado novamente e a lista de gastos sumiu / resetou para os dados iniciais.
•
Explicação: O helper viewModel() provido pelo Jetpack Compose / Lifecycle garante que o ViewModel sobreviva a recomposições e mudanças de configuração da Activity. Usar a chamada de construtor direto destrói o ciclo de vida do ViewModel.
Estrago 2: Usar mutableListOf comum no ViewModel em vez de mutableStateListOf
Código alterado: private val _gastos = mutableListOf<Gasto>() no GastosViewModel.
•
Previsão: O Jetpack Compose não conseguirá observar as mutações internas (inclusões/remoções) da lista comum do Kotlin, fazendo com que a UI não atualize automaticamente quando itens forem adicionados ou removidos.
•
O que aconteceu: Ao clicar em "Adicionar", o gasto foi adicionado na lista em memória, mas nada apareceu na tela. A lista só foi renderizada na tela muito tempo depois, quando outro estado observável (como o campo de texto da descrição) forçou uma recomposição.
•
Explicação: O mecanismo de reatividade do Compose exige tipos observáveis do Compose State runtime (mutableStateListOf ou StateFlow/LiveData). Coleções padrão do Kotlin (mutableListOf) não notificam o motor de renderização sobre modificações.
Estrago 3: Guardar o total em uma variável e atualizar apenas no adicionar()
Código alterado: var total by mutableStateOf(0.0) atualizado manualmente dentro da função adicionar().
•
Previsão: Ao filtrar a lista por categoria ou remover um gasto existente, o valor exibido no resumo total não refletirá as mudanças e ficará dessincronizado com os itens visíveis na tela.
•
O que aconteceu: Adicionaram-se três gastos e em seguida um gasto foi removido. O gasto desapareceu da lista na UI, mas o valor do total permaneceu inalterado (com o valor dos 3 gastos antigos).
•
Explicação: Dados derivados devem ser calculados dinamicamente (get()). Quando uma variável depende do estado de outros dados e não é atualizada centralizadamente em todas as mutações possíveis (remoção, filtragem, adição), surgem bugs de estado inconsistente.
Estrago 4: O ViewModel depender diretamente de uma implementação concreta (sem a interface)
Código alterado: private val repositorio = GastoRepositoryMemoria() diretamente no ViewModel sem aceitar a abstração GastoRepository.
•
Previsão: O ViewModel ficará fortemente acoplado à classe GastoRepositoryMemoria, tornando impossível trocar a fonte de dados (ex.: para GastoRepositoryDemo ou um banco de dados Room) ou injetar um repositório mock para testes unitários.
•
O que aconteceu / O que ficou mais difícil: A troca exigida na Etapa 6 exigiria modificar o código interno do GastosViewModel e recompilar a classe. Não foi mais possível alternar fontes de dados apenas ajustando a injeção de dependência/parâmetro do construtor.
•
Explicação: O princípio da Inversão de Dependência (DIP do SOLID) dita que módulos de alto nível (ViewModel) não devem depender de módulos de baixo nível (Implementação do repositório), mas sim de abstrações (Interfaces).
Estrago 5: Mover a regra de validação de volta para a camada de visão (Tela UI)
Código alterado: Button(onClick = { if (vm.descricao.isNotBlank()) { vm.adicionar() } }) na TelaGastos.kt.
•
Previsão: A lógica de negócio ficará espalhada na UI, impedindo a testabilidade automatizada sem interface gráfica e duplicando regras se outra tela ou componente precisar cadastrar gastos.
•
O que aconteceu: O app funcionou para a ação do botão, mas a regra de validação tornou-se inacessível via testes de unidade (JUnit) sem instanciar a árvore de componentes Compose. Se surgisse uma segunda tela ou atalho para cadastro, a checagem teria de ser copiada.
•
Explicação: Na arquitetura MVVM, a View deve ser o mais passiva possível ("dumb UI"). Toda validação e decisão de negócio devem residir no ViewModel, permitindo testes rápidos e reutilização de regras.