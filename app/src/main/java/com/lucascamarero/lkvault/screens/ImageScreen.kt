package com.lucascamarero.lkvault.screens

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.lucascamarero.lkvault.R
import com.lucascamarero.lkvault.models.images.EncryptedImageEntry
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

    val name = remember { mutableStateOf("") }
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
            .fillMaxWidth()
            .fillMaxHeight(0.9f)
            .padding(20.dp)
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

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

        if (images.isEmpty()) {
            item {
                Spacer(modifier = Modifier.height(200.dp))
                Text(
                    text = stringResource(id = R.string.ima_info),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 40.dp),
                    textAlign = TextAlign.Center
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
                Text(
                    text = stringResource(id = R.string.ima_nombre),
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
                        viewModel.updateImageName(
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

    if (deleteTarget != null) {
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = {
                Text(
                    stringResource(id = R.string.borrar_ima),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    textAlign = TextAlign.Center
                ) },
            text = {
                Text(
                    stringResource(id = R.string.confirm_borrar_ima),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                ) },

            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteImage(deleteTarget!!)
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

    if (showDialog) {

        AlertDialog(
            onDismissRequest = { showDialog = false },

            title = {
                Text(
                    stringResource(id = R.string.titulo_imagen_alert),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    textAlign = TextAlign.Center
                )
            },

            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

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

                    Button(
                        onClick = {
                            imagePickerLauncher.launch("image/*")
                        }
                    ) {
                        Text(stringResource(id = R.string.select_ima),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.onPrimary,
                            textAlign = TextAlign.Center)
                    }

                    if (selectedUri != null) {
                        Text(
                            stringResource(id = R.string.selected_ima),
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

                        viewModel.saveImage(
                            name.value,
                            uri,
                            key
                        )

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

    if (selectedImage != null) {

        Dialog(
            onDismissRequest = { viewModel.clearSelectedImage() },
            properties = DialogProperties(
                usePlatformDefaultWidth = false
            )
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {

                // 🔹 BOTÓN ARRIBA
                TextButton(
                    onClick = { viewModel.clearSelectedImage() },
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(16.dp)
                ) {
                    Text(
                        stringResource(id = R.string.cerrar_ima),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // 🔹 ESPACIO ENTRE BOTÓN E IMAGEN
                Spacer(modifier = Modifier.height(8.dp))

                // 🔹 IMAGEN
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {

                    selectedImage?.let { bytes ->

                        val bitmap = remember(bytes) {

                            val original = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                            try {
                                val exif = ExifInterface(bytes.inputStream())

                                val orientation = exif.getAttributeInt(
                                    ExifInterface.TAG_ORIENTATION,
                                    ExifInterface.ORIENTATION_NORMAL
                                )

                                val matrix = android.graphics.Matrix()

                                when (orientation) {
                                    ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                                    ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                                    ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                                }

                                android.graphics.Bitmap.createBitmap(
                                    original,
                                    0,
                                    0,
                                    original.width,
                                    original.height,
                                    matrix,
                                    true
                                )

                            } catch (e: Exception) {
                                original
                            }
                        }

                        bitmap?.let {

                            var scale by remember { mutableStateOf(1f) }
                            var offsetX by remember { mutableStateOf(0f) }
                            var offsetY by remember { mutableStateOf(0f) }

                            val isHorizontal = it.width > it.height

                            // 🔥 clave: ratio invertido si está rotada
                            val aspectRatio = if (isHorizontal) {
                                it.height.toFloat() / it.width.toFloat()
                            } else {
                                it.width.toFloat() / it.height.toFloat()
                            }

                            Image(
                                bitmap = it.asImageBitmap(),
                                contentDescription = "Imagen segura",
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .fillMaxWidth()
                                    .wrapContentHeight()
                                    .pointerInput(Unit) {
                                        detectTransformGestures { _, pan, zoom, _ ->

                                            val newScale = (scale * zoom).coerceIn(1f, 5f)
                                            scale = newScale

                                            if (scale > 1f) {
                                                offsetX += pan.x
                                                offsetY += pan.y
                                            } else {
                                                offsetX = 0f
                                                offsetY = 0f
                                            }
                                        }
                                    }
                                    .graphicsLayer(
                                        scaleX = scale,
                                        scaleY = scale,
                                        translationX = offsetX,
                                        translationY = offsetY,
                                        rotationZ = if (isHorizontal) 90f else 0f
                                    )
                            )
                        }
                    }
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