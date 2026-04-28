package com.lucascamarero.lkvault.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.lucascamarero.lkvault.R
import com.lucascamarero.lkvault.models.videos.EncryptedVideoEntry
import com.lucascamarero.lkvault.viewmodels.VideoViewModel
import com.lucascamarero.lkvault.viewmodels.SessionViewModel
import java.io.File

// HU31: GESTIÓN DE ARCHIVOS DE VÍDEO
// Esta pantalla permite:
// - Listar vídeos almacenados en el USB
// - Importar nuevos vídeos y cifrarlos con la Master Key
// - Visualizar vídeos de forma segura (descifrado en memoria)
// - Editar nombre de vídeo (metadata)
// - Eliminar vídeos del vault
@Composable
fun VideoScreen(
    navController: NavController,
    sessionViewModel: SessionViewModel
) {

    // ViewModel que gestiona la lógica de vídeos
    val viewModel: VideoViewModel = viewModel()

    // Master Key disponible solo en sesión
    val masterKey = sessionViewModel.masterKey

    // Estado observable de vídeos cifrados
    val videos = viewModel.videos.value

    // Vídeo actualmente descifrado (en memoria)
    val selectedVideo = viewModel.selectedVideo.value

    // Carga inicial de vídeos al entrar en pantalla
    LaunchedEffect(Unit) {
        viewModel.loadVideos()
    }

    // Control de diálogos
    var showDialog by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<String?>(null) }

    // Datos para creación de vídeo
    val name = remember { mutableStateOf("") }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }

    // Launcher que abre el selector de archivos del sistema (SAF)
    // Permite al usuario elegir un vídeo existente del almacenamiento del dispositivo.
    // El URI seleccionado se guarda en selectedUri para su posterior cifrado y almacenamiento.
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedUri = uri
        }
    }

    // Obtiene el contexto actual de la aplicación dentro de Compose
    val context = LocalContext.current

    // URI temporal donde se almacenará el vídeo capturado por la cámara.
    // Se crea antes de lanzar la cámara y permite recuperar el vídeo una vez tomada.
    var tempVideoUri by remember { mutableStateOf<Uri?>(null) }

    // Launcher que gestiona la apertura de la cámara usando Activity Result API.
    // Utiliza CaptureVideo(), que guarda la imagen directamente en el URI proporcionado.
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CaptureVideo()
    ) { success ->
        if (success && tempVideoUri != null) {
            selectedUri = tempVideoUri
        }
    }

    // Mensaje mostrado al usuario cuando el permiso de cámara es denegado.
    val mensaje = stringResource(id = R.string.permiso_camara_vid)

    // Launcher que solicita el permiso de cámara en tiempo de ejecución.
    // Utiliza ActivityResultContracts.RequestPermission(), que muestra el diálogo del sistema.
    // El resultado (permitido o denegado) se recibe en el callback.
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->

        val granted = permissions[Manifest.permission.CAMERA] == true &&
                permissions[Manifest.permission.RECORD_AUDIO] == true

        if (granted) {
            val uri = createTempVideoUri(context)
            tempVideoUri = uri
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
        }
    }

    // Estado para edición de nombre
    var editTarget by remember { mutableStateOf<EncryptedVideoEntry?>(null) }
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
                    text = stringResource(id = R.string.titulo_vid),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    style = MaterialTheme.typography.titleSmall
                )

                // Botón para añadir nuevo vídeo
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
                        contentDescription = "Añadir vídeo",
                        tint = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }

        // -------- ESTADO VACÍO --------
        if (videos.isEmpty()) {
            item {
                Spacer(modifier = Modifier.height(200.dp))
                Text(
                    text = stringResource(id = R.string.vid_info),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 40.dp),
                    textAlign = TextAlign.Center
                )
            }
        } else {

            // -------- LISTADO DE VÍDEOS --------
            items(videos) { entry ->

                VideoItem(
                    entry = entry,

                    // Descifrado bajo demanda
                    onView = {
                        val key = masterKey ?: return@VideoItem
                        viewModel.decryptVideo(entry, key)
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
                    text = stringResource(id = R.string.vid_nombre),
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
                        viewModel.updateVideoName(
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
                    stringResource(id = R.string.borrar_vid),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    textAlign = TextAlign.Center
                ) },
            text = {
                Text(
                    stringResource(id = R.string.confirm_borrar_vid),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                ) },

            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteVideo(deleteTarget!!)
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

    // -------- DIÁLOGO CREAR VÍDEO --------
    if (showDialog) {

        AlertDialog(
            onDismissRequest = { showDialog = false },

            title = {
                Text(
                    stringResource(id = R.string.titulo_video_alert),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    textAlign = TextAlign.Center
                )
            },

            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                    // Nombre del vídeo
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

                    // Selección de vídeo
                    Button(
                        onClick = {
                            videoPickerLauncher.launch("video/*")
                        }
                    ) {
                        Text(stringResource(id = R.string.select_vid),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.onPrimary,
                            textAlign = TextAlign.Center)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Grabar vídeo con la cámara
                    Button(
                        onClick = {
                            val hasCamera = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED

                            val hasAudio = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED

                            if (hasCamera && hasAudio) {
                                val uri = createTempVideoUri(context)
                                tempVideoUri = uri
                                cameraLauncher.launch(uri)
                            } else {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.CAMERA,
                                        Manifest.permission.RECORD_AUDIO
                                    )
                                )
                            }
                        }
                    ) {
                        Text(
                            text = stringResource(id = R.string.tomar_video),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }

                    // Indicador de selección
                    if (selectedUri != null) {
                        Text(
                            stringResource(id = R.string.selected_vid),
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
                        viewModel.saveVideo(
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
    if (selectedVideo != null) {

        val context = LocalContext.current

        var player by remember { mutableStateOf<androidx.media3.exoplayer.ExoPlayer?>(null) }
        var tempFile by remember { mutableStateOf<File?>(null) }

        Dialog(
            onDismissRequest = {
                player?.release()
                tempFile?.delete()
                player = null
                tempFile = null
                viewModel.clearSelectedVideo()
            },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            var isPlaying by remember { mutableStateOf(false) }
            var isPaused by remember { mutableStateOf(false) }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {

                // -------- BOTÓN CERRAR --------
                TextButton(
                    onClick = {
                        player?.release()
                        tempFile?.delete()
                        player = null
                        tempFile = null
                        viewModel.clearSelectedVideo()
                    },
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(16.dp)
                ) {
                    Text(
                        stringResource(id = R.string.cerrar_vid),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    )
                }

                // -------- CREAR PLAYER Y ARCHIVO --------
                AndroidView(
                    factory = { ctx ->

                        val playerView = androidx.media3.ui.PlayerView(ctx)

                        val exoPlayer = androidx.media3.exoplayer.ExoPlayer.Builder(ctx).build()
                        player = exoPlayer

                        playerView.player = exoPlayer
                        playerView.useController = false // usamos botones propios

                        // archivo temporal
                        val file = File.createTempFile("temp_video", ".mp4", ctx.cacheDir)
                        file.writeBytes(selectedVideo!!)
                        tempFile = file

                        val mediaItem = androidx.media3.common.MediaItem.fromUri(
                            android.net.Uri.fromFile(file)
                        )

                        exoPlayer.setMediaItem(mediaItem)
                        exoPlayer.prepare()

                        exoPlayer.addListener(object : androidx.media3.common.Player.Listener {
                            override fun onPlaybackStateChanged(state: Int) {
                                if (state == androidx.media3.common.Player.STATE_ENDED) {

                                    // mover al inicio
                                    exoPlayer.seekTo(0)

                                    // asegurarte de que NO se reproduce
                                    exoPlayer.pause()

                                    // estado UI
                                    isPlaying = false
                                    isPaused = false
                                }
                            }
                        })

                        playerView
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )

                // -------- CONTROLES --------
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // PLAY / RESUME
                    Button(
                        onClick = {

                            if (!isPlaying) {
                                player?.play()
                                isPlaying = true
                                isPaused = false

                            } else if (isPaused) {
                                player?.play()
                                isPaused = false
                            }
                        }
                    ) {
                        Text(
                            when {
                                !isPlaying -> stringResource(id = R.string.reproducir_audio)
                                isPaused -> stringResource(id = R.string.reanudar_audio)
                                else -> stringResource(id = R.string.reproduciendo_audio)
                            }
                        )
                    }

                    // PAUSE
                    Button(
                        onClick = {
                            if (isPlaying && !isPaused) {
                                player?.pause()
                                isPaused = true
                            }
                        },
                        enabled = isPlaying && !isPaused
                    ) {
                        Text(stringResource(id = R.string.pausar_audio))
                    }

                    // STOP
                    Button(
                        onClick = {
                            player?.pause()
                            player?.seekTo(0)
                            isPlaying = false
                            isPaused = false
                        },
                        enabled = isPlaying
                    ) {
                        Text(stringResource(id = R.string.detener_audio))
                    }
                }
            }
        }
    }
}

// -------- COMPONENTE ITEM --------
// Representa un vídeo individual dentro de la lista
@Composable
fun VideoItem(
    entry: EncryptedVideoEntry,
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

        // Nombre del vídeo (metadata)
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

// Crea un archivo temporal en el almacenamiento interno de la app (cache)
// y devuelve un URI seguro mediante FileProvider para ser utilizado por la cámara
fun createTempVideoUri(context: Context): Uri {

    // Crea un archivo temporal con nombre y extensión definidos dentro del directorio cache.
    // Este archivo será el destino donde la cámara guardará el vídeo capturado.
    val file = File.createTempFile(
        "temp_video",
        ".mp4",
        context.cacheDir
    )

    // Convierte el archivo en un URI seguro usando FileProvider.
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )
}