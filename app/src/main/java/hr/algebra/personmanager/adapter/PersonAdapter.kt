package hr.algebra.personmanager.adapter

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import hr.algebra.personmanager.App
import hr.algebra.personmanager.Navigable
import hr.algebra.personmanager.PERSON_KEY
import hr.algebra.personmanager.R
import hr.algebra.personmanager.dao.Person
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class PersonAdapter (
    private val context: Context,
    private val people: MutableList<Person>,
    private val navigable: Navigable
) : RecyclerView.Adapter<PersonAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle = itemView.findViewById<TextView>(R.id.tvTitle)
        private val ivImage = itemView.findViewById<ImageView>(R.id.ivImage)
        val ivDelete = itemView.findViewById<ImageView>(R.id.ivDelete)

        fun bind(person: Person) {
            Picasso.get()
                .load(File(person.picturePath!!))
                .error(R.mipmap.ic_launcher)
                .transform(RoundedCornersTransformation(50, 5))
                .into(ivImage)

            tvTitle.text = person.title
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context).inflate(R.layout.person, parent, false)
        )
    }

    override fun getItemCount(): Int = people.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val person = people[position]

        holder.ivDelete.setOnLongClickListener {
            GlobalScope.launch (Dispatchers.Main) {

                withContext(Dispatchers.IO) {
                    (context?.applicationContext as App)
                        .getPersonDao()
                        .delete(person)

                    File(person.picturePath).delete()
                }

                people.removeAt(position)
                notifyDataSetChanged()
            }
            true
        }

        holder.itemView.setOnClickListener {
            navigable.navigate(Bundle().apply {
                putLong(PERSON_KEY, person._id!!)
            })
        }

        holder.bind(person)
    }
}