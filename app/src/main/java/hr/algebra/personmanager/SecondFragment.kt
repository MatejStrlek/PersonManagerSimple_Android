package hr.algebra.personmanager

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import com.squareup.picasso.Picasso
import hr.algebra.personmanager.dao.Person
import hr.algebra.personmanager.databinding.FragmentSecondBinding
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
const val PERSON_KEY = "hr.algebra.PERSON_KEY"
private const val MIME_TYPE = "image/*"
class SecondFragment : Fragment() {

    private lateinit var person: Person
    private var _binding: FragmentSecondBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fetchPerson()
        setupListeners()
    }

    private fun fetchPerson() {
        val id = arguments?.getLong(PERSON_KEY)
        if (id != null) {
            //from database
            GlobalScope.launch (Dispatchers.Main) {
                person = withContext(Dispatchers.IO) {
                    //load data
                    (context?.applicationContext as App)
                        .getPersonDao()
                        .getPerson(id) ?: Person()
                }
                //show data
                bindPerson()
            }
        } else {
            person = Person()
            bindPerson()
        }
    }

    private fun bindPerson() {
        Picasso.get()
            .load(File(person.picturePath ?: ""))
            .error(R.mipmap.ic_launcher)
            .transform(RoundedCornersTransformation(50, 5))
            .into(binding.ivImage)

        binding.tvDate.text = person.birthDate.format(DateTimeFormatter.ISO_DATE)
        binding.etFirstName.setText(person.firstName ?: "")
        binding.etLastName.setText(person.lastName ?: "")
        binding.etTitle.setText(person.title ?: "")
    }

    private fun setupListeners() {
        binding.etFirstName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                person.firstName = text?.toString()?.trim()
            }
            override fun afterTextChanged(p0: Editable?) {}
        })

        binding.etLastName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                person.lastName = text?.toString()?.trim()
            }
            override fun afterTextChanged(p0: Editable?) {}
        })

        binding.etTitle.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                person.title = text?.toString()?.trim()
            }
            override fun afterTextChanged(p0: Editable?) {}
        })

        binding.tvDate.setOnClickListener {
            handleDate()
        }
        binding.ivImage.setOnClickListener {
            handleImage()
        }
        binding.btnCommit.setOnClickListener {
            if (formValid()) commit()
        }
    }

    private fun handleDate() {
        DatePickerDialog(
            requireContext(),
            { _, y, m,  d ->
                person.birthDate = LocalDate.of(y, m + 1, d)
                bindPerson()
            },
            person.birthDate.year,
            person.birthDate.monthValue - 1,
            person.birthDate.dayOfMonth
        ).show()
    }

    private fun handleImage() {
        Intent(Intent.ACTION_GET_CONTENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = MIME_TYPE
            callback.launch(this)
        }
    }

    private val callback = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
          if (it.resultCode == Activity.RESULT_OK && it.data != null) {
              var dir = context?.applicationContext?.getExternalFilesDir(null)
              val file = File(
                  dir, File.separator.toString() + UUID.randomUUID().toString() + ".jpg"
              )
              context?.contentResolver?.openInputStream(it.data?.data as Uri).use { inputStream ->
                  FileOutputStream(file).use { outputStream ->
                      val bitmap = BitmapFactory.decodeStream(inputStream)
                      val bos = ByteArrayOutputStream()
                      bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos)
                      outputStream.write(bos.toByteArray())
                      person.picturePath = file.absolutePath
                      bindPerson()
                  }
              }
          }
    }

    private fun formValid(): Boolean {
        var ok = true

        arrayOf(binding.etFirstName, binding.etLastName, binding.etTitle).forEach {
            if (it.text.trim().isNullOrEmpty()){
                ok = false
                it.error = getString(R.string.required_field)
            }
        }

        return ok && person.picturePath != null
    }

    private fun commit() {
        GlobalScope.launch (Dispatchers.Main) {
            withContext(Dispatchers.IO) {
                if (person._id == null)
                    (context?.applicationContext as App).getPersonDao().insert(person)
                else
                    (context?.applicationContext as App).getPersonDao().update(person)
            }
            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}