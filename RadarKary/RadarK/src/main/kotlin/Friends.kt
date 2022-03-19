import androidx.compose.runtime.MutableState
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.CoroutineScope

object Friends {
    lateinit var punti : SnapshotStateMap<Offset, Punto>
    lateinit var radius : MutableState<Float>
    lateinit var radiussave :  MutableState<Float>
    lateinit var coroutineScope: CoroutineScope
}