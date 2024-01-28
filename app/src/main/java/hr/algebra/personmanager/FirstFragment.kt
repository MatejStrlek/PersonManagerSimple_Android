package hr.algebra.personmanager

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import hr.algebra.personmanager.adapter.PersonAdapter
import hr.algebra.personmanager.databinding.FragmentFirstBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment(), Navigable {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadPeople()
        setupListeners()
    }

    private fun loadPeople() {
        //background in main thread
        GlobalScope.launch (Dispatchers.Main) {
            val people = withContext(Dispatchers.IO) {
                //load data
                (context?.applicationContext as App).getPersonDao().getPeople()
            }
            //show data
            binding.rvPeople.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = PersonAdapter(context, people, this@FirstFragment)
            }
        }
    }

    private fun setupListeners() {
        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun navigate(bundle: Bundle) {
        findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment, bundle)
    }
}