package gov.wa.wsdot.android.wsdot.ui.borderwait;

import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.arch.lifecycle.MutableLiveData;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import gov.wa.wsdot.android.wsdot.repository.BorderWaitRepository;
import gov.wa.wsdot.android.wsdot.util.network.ResourceStatus;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class BorderWaitViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantExecutor = new InstantTaskExecutorRule();
    private BorderWaitViewModel viewModel;
    private BorderWaitRepository repository;

    @Before
    public void init(){
        repository = mock(BorderWaitRepository.class);
        viewModel = new BorderWaitViewModel(repository);
    }

    @Test
    public void initSouthbound(){
        ArgumentCaptor<String> direction = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<MutableLiveData<ResourceStatus>> status = ArgumentCaptor.forClass(MutableLiveData.class);

        viewModel.init(BorderWaitViewModel.BorderDirection.SOUTHBOUND);
        verify(repository, times(1)).getBorderWaitsFor(direction.capture(), status.capture());

        assert direction.getValue().equals("southbound");
    }

    @Test
    public void initNorthbound(){
        ArgumentCaptor<String> direction = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<MutableLiveData<ResourceStatus>> status = ArgumentCaptor.forClass(MutableLiveData.class);

        viewModel.init(BorderWaitViewModel.BorderDirection.NORTHBOUND);
        verify(repository, times(1)).getBorderWaitsFor(direction.capture(), status.capture());

        assert direction.getValue().equals("northbound");
    }

    @Test
    public void forceRefresh(){
        ArgumentCaptor<Boolean> force = ArgumentCaptor.forClass(Boolean.class);
        ArgumentCaptor<MutableLiveData<ResourceStatus>> status = ArgumentCaptor.forClass(MutableLiveData.class);

        viewModel.forceRefreshBorderWaits();
        verify(repository, times(1)).refreshData(status.capture(), force.capture());

        assert force.getValue();
    }
}