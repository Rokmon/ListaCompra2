package com.mispruebas.listacompra2

import android.os.Bundle
import androidx.activity.ComponentActivity
import android.content.res.Configuration
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.mispruebas.listacompra2.db.AppDatabase
import com.mispruebas.listacompra2.db.Item
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.ui.res.stringResource

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Prueba de inspector base de datos
        lifecycleScope.launch(Dispatchers.IO){
            val itemDao = AppDatabase.getInstance(this@MainActivity).itemDao()
            val contarItem = itemDao.countAll()
            if (contarItem < 1){
                itemDao.insertItem(Item(0, "Leche", false))
                itemDao.insertItem(Item(0, "Pan", false))
                itemDao.insertItem(Item(0, "Huevos", false))
            }

        }
        setContent {
            ListaComprasUI()
        }
    }
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        // Aquí puedes verificar si el idioma ha cambiado y, si es necesario, actualizar la UI
        recreate()  // Este método reiniciará la actividad, cargando las nuevas traducciones
    }
}

@Composable
fun ListaComprasUI() {
    val llamadaCoroutina = rememberCoroutineScope()//se crea una variable para la coroutina
    val contexto = LocalContext.current//aca se recupera el contexto de la aplicacion
    val (items, setItems) = remember { mutableStateOf(emptyList<Item>()) }//se crea una variable para los items
    val (mostrarDialogo, setMostrarDialogo) = remember { mutableStateOf(false) }//
    val (nombreItem, setNombreItem) = remember { mutableStateOf("") }//

    LaunchedEffect(items) {//aca se crea un efecto para que se ejecute la coroutina
        withContext(Dispatchers.IO) {//aca se crea un contexto para la coroutina
            val dao = AppDatabase.getInstance(contexto).itemDao()//
            setItems(dao.getAllItems())//se asignan los items
        }
    }

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {//

        // Si no hay items, muestra el mensaje.
        if (items.isEmpty()) {
            Text(
                text = "No hay lista",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .align(Alignment.CenterHorizontally)
            )
        } else { // Si hay items, muestra la lista.
            LazyColumn {
                items(items) { item ->
                    ListaItemUI(item) {
                        setItems(emptyList()) // Refrescar la lista
                    }
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))//se crea un espacio
        // Muestra los botones sin importar si hay items o no.
        Row(//se crea una fila
            modifier = Modifier
                .fillMaxWidth()//
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = { setMostrarDialogo(true) }) {
                Text(text = stringResource(id = R.string.add_item))
            }

            // Botón de borrar todo
            Button(onClick = {
                llamadaCoroutina.launch(Dispatchers.IO) {
                    val dao = AppDatabase.getInstance(contexto).itemDao()
                    dao.deleteAll()
                    setItems(emptyList())
                }
            }) {
                Text(text = stringResource(id = R.string.delete_all))
            }
        }

        if (mostrarDialogo) {
            DialogoAgregarItem(nombreItem, setNombreItem) {
                llamadaCoroutina.launch(Dispatchers.IO) {
                    val dao = AppDatabase.getInstance(contexto).itemDao()
                    dao.insertItem(Item(0, nombreItem, false))
                    setItems(dao.getAllItems())
                    setMostrarDialogo(false) // Cerrar el cuadro de diálogo después de agregar el item
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoAgregarItem(nombreItem: String, setNombreItem: (String) -> Unit, agregarItem: () -> Unit) {
    AlertDialog(
        onDismissRequest = { /* Si quieres hacer algo al cerrar el diálogo, puedes añadirlo aquí */ },
        title = { Text(text = "Agregar nuevo item") },
        text = {
            OutlinedTextField(
                value = nombreItem,
                onValueChange = { setNombreItem(it) },
                label = { Text("Nombre del item") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (nombreItem.isNotBlank()) {
                        agregarItem()
                    }
                }
            ) {
                Text("Agregar")
            }
        },
        dismissButton = {
            TextButton(onClick = {  }) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun ListaItemUI(item: Item,guardar:()->Unit = {}){//se crea la funcion composable para mostrar un item
    val llamadaCoroutina = rememberCoroutineScope()//se crea una variable para la coroutina
    val contexto = LocalContext.current//aca se recupera el contexto de la aplicacion
    Row(
        modifier = Modifier
            .fillMaxWidth()//aca se asigna el ancho maximo
            .padding(vertical = 8.dp, horizontal = 16.dp)//aca se asigna el padding
            .border(1.dp, Color.Gray)
            .background(Color.White)

    ){
        if(item.comprada){
            Icon(Icons.Filled.Check, contentDescription = "Listo",modifier=Modifier.clickable {
                llamadaCoroutina.launch(Dispatchers.IO){
                    val dao = AppDatabase.getInstance(contexto).itemDao()
                    item.comprada = false
                    dao.updateItem(item)
                    guardar()
                }
            })//se muestra el icono de check
        }else{
            Icon(Icons.Filled.ShoppingCart, contentDescription = "Comprar",modifier = Modifier.clickable {
                llamadaCoroutina.launch(Dispatchers.IO){
                    val dao = AppDatabase.getInstance(contexto).itemDao()
                    item.comprada = true
                    dao.updateItem(item)
                    guardar()
                }
            })//se muestra el icono de comprar
        }
        Spacer(modifier = Modifier
            .width(16.dp)//se le asigna un ancho
            )//se crea un espacio

        Text(item.nombre,modifier=Modifier.weight(2f))//se muestra el nombre del item
        Icon(Icons.Filled.Delete, contentDescription = "Eliminar",modifier = Modifier.clickable {//se muestra el icono de delete
            llamadaCoroutina.launch(Dispatchers.IO){
                val dao = AppDatabase.getInstance(contexto).itemDao()
                dao.deleteItem(item)
                guardar()
            }
        })
    }
}


@Preview(showBackground = true)
@Composable
fun ListaItemUIPreview(){//aca se puede viualizar el item de prueba de la funcion anterior

    Column {
        var item = Item(0, "Leche", false)//se crea un item de prueba
        ListaItemUI(item)//se muestra el item de prueba

        var item2 = Item(0, "Pan", true)//se crea un item de prueba
        ListaItemUI(item2)//se muestra el item de prueba

        var item3 = Item(0, "Huevos", false)//se crea un item de prueba
        ListaItemUI(item3)//se muestra el item de prueba
    }

}


