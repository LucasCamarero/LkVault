package com.lucascamarero.lkvault.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.lucascamarero.lkvault.R
import com.lucascamarero.lkvault.models.docs.EncryptedDocEntry
import com.lucascamarero.lkvault.viewmodels.DocViewModel
import com.lucascamarero.lkvault.viewmodels.SessionViewModel
import java.io.File
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

// HU-32: GESTIÓN DE DOCUMENTOS
// Esta pantalla permite:
// - Listar documentos almacenados en el USB
// - Importar nuevos documentos y cifrarlos con la Master Key
// - Visualizar documentos de forma segura (descifrado en memoria)
// - Editar nombre de documento (metadata)
// - Eliminar documentos del vault
@Composable
fun DocScreen(
    navController: NavController,
    sessionViewModel: SessionViewModel
) {

    // ViewModel que gestiona la lógica de documentos
    val viewModel: DocViewModel = viewModel()

    // Master Key disponible solo en sesión
    val masterKey = sessionViewModel.masterKey

    // Estado observable de documentos cifrados
    val docs = viewModel.docs.value

    // Documento actualmente descifrado (en memoria)
    val selectedDoc = viewModel.selectedDoc.value

    // Carga inicial de documentos al entrar en pantalla
    LaunchedEffect(Unit) {
        viewModel.loadDocs()
    }

    // Control de diálogos
    var showDialog by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<String?>(null) }

    // Datos para creación de documento
    val name = remember { mutableStateOf("") }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }

    // Selector de documento desde el sistema (SAF)
    val docPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedUri = uri
        }
    }

    // Estado para edición de nombre
    var editTarget by remember { mutableStateOf<EncryptedDocEntry?>(null) }
    val editName = remember { mutableStateOf("") }

    // -------- LISTADO PRINCIPAL --------
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.9f)
            .padding(20.dp)
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // -------- HEADER --------
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 25.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.titulo_doc),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    style = MaterialTheme.typography.titleSmall
                )

                // Botón para añadir nuevo documento
                IconButton(
                    onClick = { showDialog = true },
                    modifier = Modifier
                        .size(60.dp)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Añadir documento",
                        tint = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }

        // -------- ESTADO VACÍO --------
        if (docs.isEmpty()) {
            item {
                Spacer(modifier = Modifier.height(200.dp))
                Text(
                    text = stringResource(id = R.string.doc_info),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 40.dp),
                    textAlign = TextAlign.Center
                )
            }
        } else {

            // -------- LISTADO DE DOCUMENTOS --------
            items(docs) { entry ->

                DocItem(
                    entry = entry,

                    // Descifrado bajo demanda
                    onView = {
                        val key = masterKey ?: return@DocItem
                        viewModel.decryptDoc(entry, key)
                    },

                    // Preparar eliminación
                    onDelete = {
                        deleteTarget = entry.id
                    },

                    // Preparar edición
                    onEdit = {
                        editTarget = entry
                        editName.value = entry.name
                    }
                )
            }
        }
    }

    // -------- DIÁLOGO EDITAR NOMBRE --------
    if (editTarget != null) {

        AlertDialog(
            onDismissRequest = { editTarget = null },

            title = {
                Text(
                    text = stringResource(id = R.string.doc_nombre),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    textAlign = TextAlign.Center
                )
            },

            text = {
                OutlinedTextField(
                    value = editName.value,
                    onValueChange = { editName.value = it },
                    label = {
                        Text(
                            stringResource(id = R.string.con_name),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            style = MaterialTheme.typography.labelLarge
                        )},
                    singleLine = true,
                    shape = RoundedCornerShape(26.dp),
                    textStyle = MaterialTheme.typography.labelLarge,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.secondaryContainer,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )
            },

            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.updateDocName(
                            editTarget!!.id,
                            editName.value
                        )
                        editTarget = null
                    },
                    enabled = editName.value.isNotBlank(),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.secondaryContainer,
                        disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                ) {
                    Text(stringResource(id = R.string.con_save_button),
                        style = MaterialTheme.typography.bodySmall)
                }
            },

            dismissButton = {
                TextButton(
                    onClick = { editTarget = null }
                ) {
                    Text(stringResource(id = R.string.con_cancel_button),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondaryContainer)
                }
            }
        )
    }

    // -------- DIÁLOGO ELIMINAR --------
    if (deleteTarget != null) {
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = {
                Text(
                    stringResource(id = R.string.borrar_doc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    textAlign = TextAlign.Center
                ) },
            text = {
                Text(
                    stringResource(id = R.string.confirm_borrar_doc),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                ) },

            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteDoc(deleteTarget!!)
                        deleteTarget = null
                    }
                ) {
                    Text(stringResource(id = R.string.con_del),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondaryContainer)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { deleteTarget = null }
                ) {
                    Text(
                        stringResource(id = R.string.con_cancel_button),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondaryContainer
                    )
                }
            }
        )
    }

    // -------- DIÁLOGO CREAR DOCUMENTO --------
    if (showDialog) {

        AlertDialog(
            onDismissRequest = { showDialog = false },

            title = {
                Text(
                    stringResource(id = R.string.titulo_doc_alert),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    textAlign = TextAlign.Center
                )
            },

            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                    // Nombre del documento
                    OutlinedTextField(
                        value = name.value,
                        onValueChange = { name.value = it },
                        label = {
                            Text(
                                stringResource(id = R.string.con_name),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                style = MaterialTheme.typography.labelLarge
                            )},
                        singleLine = true,
                        shape = RoundedCornerShape(26.dp),
                        textStyle = MaterialTheme.typography.labelLarge,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.secondaryContainer,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Selección de documento
                    Button(
                        onClick = {
                            docPickerLauncher.launch("application/pdf")
                        }
                    ) {
                        Text(stringResource(id = R.string.select_doc),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.onPrimary,
                            textAlign = TextAlign.Center)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Indicador de selección
                    if (selectedUri != null) {
                        Text(
                            stringResource(id = R.string.selected_doc),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center)

                        Spacer(modifier = Modifier.height(5.dp))
                    }
                }
            },

            confirmButton = {
                TextButton(
                    onClick = {
                        val key = masterKey ?: return@TextButton
                        val uri = selectedUri ?: return@TextButton

                        // Guardado cifrado
                        viewModel.saveDoc(
                            name.value,
                            uri,
                            key
                        )

                        uri.path?.let {
                            File(it).delete()
                        }

                        name.value = ""
                        selectedUri = null
                        showDialog = false
                    },
                    enabled = name.value.isNotBlank() && selectedUri != null,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.secondaryContainer,
                        disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                ) {
                    Text(stringResource(id = R.string.con_save_button),
                        style = MaterialTheme.typography.bodySmall)
                }
            },

            dismissButton = {
                TextButton(
                    onClick = {
                        name.value = ""
                        selectedUri = null
                        showDialog = false
                    }) {
                    Text(stringResource(id = R.string.con_cancel_button),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondaryContainer)
                }
            }
        )
    }

    // -------- VISUALIZACIÓN SEGURA --------
    if (selectedDoc != null) {

        val context = LocalContext.current
        var pages by remember { mutableStateOf<List<android.graphics.Bitmap>>(emptyList()) }

        LaunchedEffect(selectedDoc) {

            val file = File.createTempFile("temp_doc", ".pdf", context.cacheDir)
            file.writeBytes(selectedDoc!!)

            val descriptor = android.os.ParcelFileDescriptor.open(
                file,
                android.os.ParcelFileDescriptor.MODE_READ_ONLY
            )

            val renderer = android.graphics.pdf.PdfRenderer(descriptor)

            val displayMetrics = context.resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels

            val list = mutableListOf<android.graphics.Bitmap>()

            for (i in 0 until renderer.pageCount) {

                val page = renderer.openPage(i)

                val scale = screenWidth.toFloat() / page.width.toFloat()

                val bitmap = android.graphics.Bitmap.createBitmap(
                    screenWidth,
                    (page.height * scale).toInt(),
                    android.graphics.Bitmap.Config.ARGB_8888
                )

                val matrix = android.graphics.Matrix().apply {
                    postScale(scale, scale)
                }

                page.render(
                    bitmap,
                    null,
                    matrix,
                    android.graphics.pdf.PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                )

                list.add(bitmap)
                page.close()
            }

            renderer.close()
            descriptor.close()

            pages = list
        }

        Dialog(
            onDismissRequest = { viewModel.clearSelectedDoc() },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {

                // BOTÓN CERRAR
                TextButton(
                    onClick = { viewModel.clearSelectedDoc() },
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(16.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.cerrar_doc),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // CONTENIDO PDF
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(pages) { bitmap ->
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "PDF page",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }

}

// -------- COMPONENTE ITEM --------
// Representa un documento individual dentro de la lista
@Composable
fun DocItem(
    entry: EncryptedDocEntry,
    onView: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(
                MaterialTheme.colorScheme.primaryContainer,
                RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {

        // Nombre del documento (metadata)
        Text(
            text = entry.name.uppercase(),
            color = MaterialTheme.colorScheme.onPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // Acciones disponibles
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(onClick = onEdit) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Editar",
                    tint = MaterialTheme.colorScheme.secondaryContainer
                )
            }

            IconButton(onClick = onView) {
                Icon(
                    Icons.Default.Visibility,
                    contentDescription = "Ver",
                    tint = MaterialTheme.colorScheme.secondaryContainer
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = MaterialTheme.colorScheme.secondaryContainer
                )
            }
        }
    }
}