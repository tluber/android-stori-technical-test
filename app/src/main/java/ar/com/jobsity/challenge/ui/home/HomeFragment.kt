package ar.com.jobsity.challenge.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import ar.com.jobsity.challenge.databinding.FragmentHomeBinding
import ar.com.jobsity.challenge.utils.GridSpacingItemDecoration
import ar.com.jobsity.challenge.utils.extensions.px
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val viewModel: HomeViewModel by viewModels()
    private val showAdapter = ShowAdapter()
    private val columns = 3
    private var currentPage = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.showList.layoutManager = GridLayoutManager(this.context, columns)
        binding.showList.addItemDecoration(GridSpacingItemDecoration(columns, 8.px, true))
        binding.showList.adapter = showAdapter
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.homeUiState.flowWithLifecycle(lifecycle).collect {
                        when (it) {
                            is HomeUiState.Loading -> {
                                binding.loadingView.visibility = View.VISIBLE
                            }

                            is HomeUiState.Error -> {
                                binding.loadingView.visibility = View.GONE
                            }

                            is HomeUiState.Success -> {
                                binding.loadingView.visibility = View.GONE
                                showAdapter.refresh(it.showList)
                                binding.previousButton.isEnabled = currentPage > 0
                                val page = currentPage + 1
                                binding.pageNumber.text = "Page $page"
                            }
                        }
                    }
                }
            }
        }

        binding.nextButton.setOnClickListener {
            currentPage++
            viewModel.getShows(currentPage)
        }
        binding.previousButton.setOnClickListener {
            currentPage--
            viewModel.getShows(currentPage)
        }
    }
}