import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.df4l.liftaz.databinding.BottomSheetMangerBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

// Dans /app/src/main/java/com/df4l/liftaz/manger/AjouterRepasBottomSheetFragment.kt
class AjouterRepasBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetMangerBinding? = null

    // Cette propriété est valide uniquement entre onCreateView et onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetMangerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Vous pouvez ajouter ici la logique pour votre Bottom Sheet,
        // par exemple, les listeners pour les onglets, la barre de recherche, etc.
        // Exemple :
        // binding.btnSaveMeal.setOnClickListener {
        //     // Logique pour sauvegarder le repas
        //     dismiss() // Ferme le Bottom Sheet
        // }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        // Le Tag est utile pour trouver le fragment si nécessaire
        const val TAG = "AjouterRepasBottomSheetFragment"
    }
}