package com.o7solutions.freelancing_bot.Admin.ui.gallery


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.o7solutions.freelancing_bot.data_classes.User
import com.o7solutions.freelancing_bot.databinding.FragmentUserListBinding
import com.o7solutions.freelancing_bot.databinding.ItemUserAdminBinding

class UserListFragment : Fragment() {

    private var _binding: FragmentUserListBinding? = null
    private val binding get() = _binding
    private var roleFilter: Int? = null
    private var pendingList: List<User>? = null

    companion object {
        fun newInstance(role: Int?): UserListFragment {
            val fragment = UserListFragment()
            val args = Bundle()
            if (role != null) args.putInt("ROLE_KEY", role)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentUserListBinding.inflate(inflater, container, false)
        roleFilter = arguments?.let { if (it.containsKey("ROLE_KEY")) it.getInt("ROLE_KEY") else null }
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // If data arrived before the view was ready, load it now
        pendingList?.let { refreshData(it) }
    }

    fun refreshData(fullList: List<User>) {
        // Store the list in case the binding isn't ready yet
        pendingList = fullList

        // CRASH FIX: Only access binding if it is not null
        _binding?.let { b ->
            val filtered = if (roleFilter == null) fullList else fullList.filter { it.role == roleFilter }
            b.userRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            b.userRecyclerView.adapter = UserAdapter(filtered)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class UserAdapter(private val list: List<User>) : RecyclerView.Adapter<UserAdapter.UserVH>() {

    class UserVH(val b: ItemUserAdminBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserVH {
        return UserVH(ItemUserAdminBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: UserVH, position: Int) {
        val user = list[position]
        holder.b.userNameTv.text = user.name
        holder.b.userEmailTv.text = user.email
        holder.b.roleChip.text = when(user.role) {
            2 -> "Admin"
            0 -> "Employer"
            1 -> "Seeker"
            else -> "User"
        }
    }

    override fun getItemCount() = list.size
}