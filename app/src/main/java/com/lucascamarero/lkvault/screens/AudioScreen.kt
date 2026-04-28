package com.lucascamarero.lkvault.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
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
import androidx.compose.material.icons.filled.Headphones
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.lucascamarero.lkvault.R
import com.lucascamarero.lkvault.models.audios.EncryptedAudioEntry
import com.lucascamarero.lkvault.viewmodels.AudioViewModel
import com.lucascamarero.lkvault.viewmodels.SessionViewModel
import java.io.File

// HU-30: GESTIÓN DE ARCHIVOS DE AUDIO
// Esta pantalla permite:
// - Listar audios almacenados en el USB
// - Importar nuevos audios y cifrarlos con la Master Key
// - Visualizar audios de forma segura (descifrado en memoria)
// - Editar nombre de audio (metadata)
// - Eliminar audios del vault
@Composable
fun AudioScreen(
    navController: NavController,
    sessionViewModel: SessionViewModel
) {

    // ViewModel que gestiona la lógica de audios
    val viewModel: AudioViewModel = viewModel()

    // Master Key disponible solo en sesión
    val masterKey = sessionViewModel.masterKey

    // Estado observable de audios cifrados
    val audios = viewModel.audios.value

    // Audio actualmente descifrado (en memoria)
    val selectedAudio = viewModel.selectedAudio.value

    // Carga inicial de audios al entrar en pantalla
    LaunchedEffect(Unit) {
        viewModel.loadAudios()
    }

    // Control de diálogos
    var showDialog by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<String?>(null) }

    // Datos para creación de audio
    val name = remember { mutableStateOf("") }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }

    // Selector de audio desde el sistema (SAF)
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedUri = uri
        }
    }

    // Obtiene el contexto actual de la aplicación dentro de Compose
    val context = LocalContext.current

    // Indica si actualmente se está grabando un audio.
    // Se utiliza para alternar entre iniciar y detener la grabación.
    var isRecording by remember { mutableStateOf(false) }

    // Instancia activa de MediaRecorder.
    // Permite controlar el ciclo de vida de la grabación (start, stop, release).
    var recorder by remember { mutableStateOf<MediaRecorder?>(null) }

    // Archivo temporal donde se almacena el audio grabado antes de ser cifrado.
    // Se genera en cache y posteriormente se convierte en Uri para su procesamiento.
    var tempFile by remember { mutableStateOf<File?>(null) }

    // Mensaje mostrado al usuario cuando el permiso de grabación es denegado.
    val mensaje = stringResource(id = R.string.permiso_grabadora)

    // Launcher encargado de solicitar el permiso RECORD_AUDIO en tiempo de ejecución.
    // Si el usuario concede el permiso, inicia la grabación de audio.
    // En caso contrario, muestra un mensaje informativo.
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {

            startRecording(context) { rec, file ->
                recorder = rec
                tempFile = file
                isRecording = true
            }

        } else {
            Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
        }
    }

    // Estado para edición de nombre
    var editTarget by remember { mutableStateOf<EncryptedAudioEntry?>(null) }
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
                    text = stringResource(id = R.string.titulo_aud),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    style = MaterialTheme.typography.titleSmall
                )

                // Botón para añadir nuevo audio
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
                        contentDescription = "Añadir audio",
                        tint = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }

        // -------- ESTADO VACÍO --------
        if (audios.isEmpty()) {
            item {
                Spacer(modifier = Modifier.height(200.dp))
                Text(
                    text = stringResource(id = R.string.aud_info),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 40.dp),
                    textAlign = TextAlign.Center
                )
            }
        } else {

            // -------- LISTADO DE IMÁGENES --------
            items(audios) { entry ->

                AudioItem(
                    entry = entry,

                    // Descifrado bajo demanda
                    onView = {
                        val key = masterKey ?: return@AudioItem
                        viewModel.decryptAudio(entry, key)
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
                    text = stringResource(id = R.string.aud_nombre),
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
                        viewModel.updateAudioName(
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
                    stringResource(id = R.string.borrar_aud),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    textAlign = TextAlign.Center
                ) },
            text = {
                Text(
                    stringResource(id = R.string.confirm_borrar_aud),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                ) },

            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAudio(deleteTarget!!)
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

    // -------- DIÁLOGO CREAR AUDIO --------
    if (showDialog) {

        AlertDialog(
            onDismissRequest = { showDialog = false },

            title = {
                Text(
                    stringResource(id = R.string.titulo_audio_alert),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    textAlign = TextAlign.Center
                )
            },

            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                    // Nombre del audio
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

                    // Selección de audio
                    Button(
                        onClick = {
                            audioPickerLauncher.launch("audio/*")
                        }
                    ) {
                        Text(stringResource(id = R.string.select_aud),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.onPrimary,
                            textAlign = TextAlign.Center)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Grabar un audio con la grabadora
                    Button(
                        onClick = {

                            val permission = Manifest.permission.RECORD_AUDIO

                            if (!isRecording) {

                                if (ContextCompat.checkSelfPermission(
                                        context,
                                        permission
                                    ) == PackageManager.PERMISSION_GRANTED
                                ) {

                                    startRecording(context) { rec, file ->
                                        recorder = rec
                                        tempFile = file
                                        isRecording = true
                                    }

                                } else {
                                    permissionLauncher.launch(permission)
                                }

                            } else {

                                recorder?.apply {
                                    stop()
                                    release()
                                }

                                recorder = null
                                isRecording = false

                                tempFile?.let {
                                    selectedUri = Uri.fromFile(it)
                                }
                            }
                        }
                    ) {
                        Text(
                            if (isRecording) stringResource(id = R.string.detener_audio) else stringResource(id = R.string.grabar_audio),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }

                    // Indicador de selección
                    if (selectedUri != null) {
                        Text(
                            stringResource(id = R.string.selected_aud),
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
                        viewModel.saveAudio(
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

    // -------- REPRODUCCIÓN SEGURA --------
    if (selectedAudio != null) {

        val context = LocalContext.current

        Dialog(
            onDismissRequest = {
                viewModel.clearSelectedAudio()
            },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {

            var isPlaying by remember { mutableStateOf(false) }
            var isPaused by remember { mutableStateOf(false) }
            var mediaPlayer by remember { mutableStateOf<android.media.MediaPlayer?>(null) }
            var tempFile by remember { mutableStateOf<File?>(null) }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                // -------- BOTÓN CERRAR --------
                TextButton(
                    onClick = {
                        mediaPlayer?.release()
                        mediaPlayer = null
                        tempFile?.delete()
                        tempFile = null
                        isPlaying = false
                        isPaused = false
                        viewModel.clearSelectedAudio()
                    },
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(16.dp)
                ) {
                    Text(
                        stringResource(id = R.string.cerrar_aud),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    )
                }

                // -------- ICONO --------
                Icon(
                    imageVector = Icons.Default.Headphones,
                    contentDescription = "Audio",
                    tint = Color.White,
                    modifier = Modifier.size(120.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // -------- CONTROLES EN VERTICAL --------
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // PLAY / RESUME
                    Button(
                        onClick = {

                            if (!isPlaying) {

                                val file = File.createTempFile(
                                    "temp_audio",
                                    ".m4a",
                                    context.cacheDir
                                )

                                file.writeBytes(selectedAudio!!)
                                tempFile = file

                                val player = android.media.MediaPlayer().apply {
                                    setDataSource(file.absolutePath)
                                    prepare()
                                    start()
                                }

                                mediaPlayer = player
                                isPlaying = true
                                isPaused = false

                                player.setOnCompletionListener {
                                    isPlaying = false
                                    isPaused = false
                                    player.release()
                                    tempFile?.delete()
                                    tempFile = null
                                }

                            } else if (isPaused) {
                                mediaPlayer?.start()
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
                                mediaPlayer?.pause()
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
                            mediaPlayer?.stop()
                            mediaPlayer?.release()
                            mediaPlayer = null
                            tempFile?.delete()
                            tempFile = null
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
// Representa un audio individual dentro de la lista
@Composable
fun AudioItem(
    entry: EncryptedAudioEntry,
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

        // Nombre del audio (metadata)
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
                    Icons.Default.Headphones,
                    contentDescription = "Escuchar",
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

// Inicia una grabación de audio utilizando el micrófono del dispositivo.
fun startRecording(
    context: Context,
    onRecorderReady: (MediaRecorder, File) -> Unit
) {

    // Crea un archivo temporal en cache donde se almacenará el audio grabado
    val file = File.createTempFile(
        "temp_audio",
        ".m4a",
        context.cacheDir
    )

    // Configura MediaRecorder para capturar audio desde el micrófono
    val recorder = MediaRecorder().apply {

        // Fuente de audio: micrófono del dispositivo
        setAudioSource(MediaRecorder.AudioSource.MIC)

        // Formato de salida: contenedor MPEG-4 (.m4a)
        setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)

        // Codificador de audio: AAC (eficiente y ampliamente soportado)
        setAudioEncoder(MediaRecorder.AudioEncoder.AAC)

        // Ruta del archivo donde se guardará la grabación
        setOutputFile(file.absolutePath)

        // Prepara el recorder (asigna recursos internos)
        prepare()

        // Inicia la grabación
        start()
    }

    // Devuelve el recorder activo y el archivo generado para su gestión externa
    onRecorderReady(recorder, file)
}