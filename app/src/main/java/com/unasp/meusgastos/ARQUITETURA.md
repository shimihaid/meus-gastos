ARQUITETURA.md — Análise da Arquitetura MVVM (App Meus Gastos)
1. Mudança na Fonte de Dados (API vs Memória)
   •
   Arquivos que precisam mudar / serem criados:
   ◦
   Apenas a camada do Model: deve ser criada uma nova classe implementando a interface GastoRepository (ex.: GastoRepositoryApi), que faz as chamadas HTTP para o backend.
   ◦
   Apenas 1 linha de instanciação na injeção de dependência do ViewModel para passar a nova implementação do repositório.
   •
   Arquivos que com certeza NÃO mudam:
   ◦
   Nenhum arquivo da UI (TelaGastos.kt, FormularioGasto, LinhaGasto, etc.): A tela consome apenas dados e eventos expostos pelo ViewModel.
   ◦
   As regras de negócio no ViewModel (GastosViewModel.kt): Toda a lógica de filtragem, formatação de valor, controle de estado e validação continua idêntica, pois o ViewModel depende estritamente da abstração GastoRepository.
2. Por que o Total é Calculado (get()) e Não Guardado?
   O total é uma propriedade derivada. Calculá-lo dinamicamente através de um getter customizado (val total get() = gastosVisiveis.sumOf { it.valor }) garante uma única fonte da verdade (single source of truth), mantendo-o sempre sincronizado com a lista de gastos visíveis.
   •
   Bug concreto se fosse uma variável atualizada manualmente (var total): Se o total fosse uma variável manual atualizada apenas no método adicionar(), ao aplicar um filtro por categoria (ex: selecionar "Alimentação"), a lista exibida na tela encolheria para mostrar apenas os gastos daquela categoria, mas o valor do total continuaria mostrando a soma de todos os gastos do app (ou um valor desatualizado). Da mesma forma, se um item fosse removido da lista ou o repositório fosse sincronizado, o desenvolvedor teria que lembrar de atualizar manualmente a variável total em cada um desses pontos. Qualquer esquecimento geraria uma dessincronização visual grave.
3. Desacoplamento do FormularioGasto do ViewModel
   O componente FormularioGasto é um composable stateless (sem estado próprio nem dependência direta da classe GastosViewModel). Em vez de receber a instância inteira do GastosViewModel, ele recebe apenas os valores primitivos que precisa exibir (descricao, valorTexto, categoria, erro) e callbacks com as ações do usuário (aoDigitarDescricao, aoAdicionar, etc.).
   •
   O que isso permitiu fazer que não seria possível se recebesse o ViewModel:
   i.
   Desenhar e testar Previews no Android Studio sem rodar a aplicação inteira: Conseguimos criar a função @Preview fun FormularioComErroPreview() injetando uma mensagem de erro fictícia ("O valor precisa ser um número") diretamente nos parâmetros do composable. Se o componente dependesse do GastosViewModel ou do ecossistema Android/ViewModel context, a renderização de Preview seria impossível ou exigiria mocks complexos.
   ii.
   Reutilização e Testabilidade: O componente tornou-se 100% modular, podendo ser testado unitariamente e reutilizado em qualquer outra parte do app ou em diferentes telas sem estar amarrado à regra de negócio específica do GastosViewModel.