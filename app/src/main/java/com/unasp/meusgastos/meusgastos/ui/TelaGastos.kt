package br.unasp.meusgastos.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import br.unasp.meusgastos.model.Categoria
import br.unasp.meusgastos.model.Gasto
import br.unasp.meusgastos.viewmodel.GastosViewModel
@Composable
fun TelaGastos(
    vm: GastosViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {

        Text("Meus Gastos", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        FormularioGasto(
            descricao = vm.descricao,
            valorTexto = vm.valorTexto,
            categoria = vm.categoriaSelecionada,
            erro = vm.erro,
            aoDigitarDescricao = vm::aoDigitarDescricao,
            aoDigitarValor = vm::aoDigitarValor,
            aoSelecionarCategoria = vm::aoSelecionarCategoria,
            aoAdicionar = vm::adicionar
        )

        FiltroCategorias(selecionada = vm.filtro, aoFiltrar = vm::aoFiltrar)

        ResumoTotal(total = vm.total, quantidade = vm.quantidade)

        LazyColumn {
            items(vm.gastosVisiveis) { gasto ->
                LinhaGasto(gasto = gasto, aoRemover = { vm.remover(gasto.id) })
            }
        }
    }
}
