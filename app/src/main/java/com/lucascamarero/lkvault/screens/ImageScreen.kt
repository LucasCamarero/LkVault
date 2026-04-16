package com.lucascamarero.lkvault.screens

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.lucascamarero.lkvault.R
import com.lucascamarero.lkvault.models.EncryptedImageEntry
import com.lucascamarero.lkvault.viewmodels.ImageViewModel
import com.lucascamarero.lkvault.viewmodels.SessionViewModel

@Composable
fun ImageScreen(
    navController: NavController,
    sessionViewModel: SessionViewModel
) {

    val viewModel: ImageViewModel = viewModel()

    val masterKey = sessionViewModel.masterKey
    val images = viewModel.images.value
    val selectedImage = viewModel.selectedImage.value

    LaunchedEffect(Unit) {
        viewModel.loadImages()
    }

    var showDialog by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<String?>(null) }

    // nombre imagen
    val name = remember { mutableStateOf("") }

    // URI seleccionada (luego conectarás con galería)
    var selectedUri by remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedUri = uri
        }
    }

    var editTarget by remember { mutableStateOf<EncryptedImageEntry?>(null) }
    val editName = remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
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
                    text = stringResource(id = R.string.titulo_ima),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    style = MaterialTheme.typography.titleSmall
                )

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
                        contentDescription = "Añadir imagen",
                        tint = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }

        // -------- LISTA --------
        if (images.isEmpty()) {
            item {
                Spacer(modifier = Modifier.height(200.dp))
                Text(
                    text = "No hay imágenes guardadas",
                    color = MaterialTheme.colorScheme.primaryContainer,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 40.dp)
                )
            }
        } else {
            items(images) { entry ->

                ImageItem(
                    entry = entry,
                    onView = {
                        val key = masterKey ?: return@ImageItem
                        viewModel.decryptImage(entry, key)
                    },
                    onDelete = {
                        deleteTarget = entry.id
                    },
                    onEdit = {
                        editTarget = entry
                        editName.value = entry.name
                    }
                )
            }
        }
    }

    if (editTarget != null) {

        AlertDialog(
            onDismissRequest = { editTarget = null },

            title = {
                Text("Editar nombre")
            },

            text = {
                OutlinedTextField(
                    value = editName.value,
                    onValueChange = { editName.value = it },
                    label = { Text("Nuevo nombre") },
                    singleLine = true
                )
            },

            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.updateImageName(
                            editTarget!!.id,
                            editName.value
                        )
                        editTarget = null
                    },
                    enabled = editName.value.isNotBlank()
                ) {
                    Text("Guardar")
                }
            },

            dismissButton = {
                TextButton(onClick = { editTarget = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // -------- DELETE DIALOG --------
    if (deleteTarget != null) {
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Eliminar imagen") },
            text = { Text("¿Seguro que quieres eliminarla?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteImage(deleteTarget!!)
                    deleteTarget = null
                }) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // -------- ADD IMAGE DIALOG --------
    if (showDialog) {

        AlertDialog(
            onDismissRequest = { showDialog = false },

            title = {
                Text(
                    "Nueva imagen",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },

            text = {
                Column {

                    OutlinedTextField(
                        value = name.value,
                        onValueChange = { name.value = it },
                        label = { Text("Nombre") },
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            imagePickerLauncher.launch("image/*")
                        }
                    ) {
                        Text("Seleccionar imagen")
                    }

                    if (selectedUri != null) {
                        Text("Imagen seleccionada")
                    }
                }
            },

            confirmButton = {
                TextButton(
                    onClick = {
                        val key = masterKey ?: return@TextButton
                        val uri = selectedUri ?: return@TextButton

                        viewModel.saveImage(
                            name.value,
                            uri,
                            key
                        )

                        name.value = ""
                        selectedUri = null
                        showDialog = false
                    },
                    enabled = name.value.isNotBlank() && selectedUri != null
                ) {
                    Text("Guardar")
                }
            },

            dismissButton = {
                TextButton(onClick = {
                    name.value = ""
                    selectedUri = null
                    showDialog = false
                }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // -------- VISUALIZACIÓN --------
    if (selectedImage != null) {

        Dialog(
            onDismissRequest = { viewModel.clearSelectedImage() },
            properties = DialogProperties(
                usePlatformDefaultWidth = false  // 🔥 CLAVE
            )
        ) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {

                selectedImage?.let { bytes ->

                    val bitmap = remember(bytes) {
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    }

                    bitmap?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = "Imagen segura",
                            modifier = Modifier
                                .fillMaxSize()
                        )
                    }
                }

                // Botón cerrar
                TextButton(
                    onClick = { viewModel.clearSelectedImage() },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Text(
                        "Cerrar",
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun ImageItem(
    entry: EncryptedImageEntry,
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

        Text(
            text = entry.name.uppercase(),
            color = MaterialTheme.colorScheme.onPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {

            IconButton(onClick = onView) {
                Icon(
                    Icons.Default.Visibility,
                    contentDescription = "Ver",
                    tint = MaterialTheme.colorScheme.secondaryContainer
                )
            }

            IconButton(onClick = onEdit) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Editar",
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