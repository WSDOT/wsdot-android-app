package gov.wa.wsdot.android.wsdot.ui.borderwait;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import gov.wa.wsdot.android.wsdot.database.borderwaits.BorderWaitEntity;
import gov.wa.wsdot.android.wsdot.repository.BorderWaitRepository;
import gov.wa.wsdot.android.wsdot.util.network.ResourceStatus;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(MockitoJUnitRunner.class)
public class BorderWaitViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantExecutor = new InstantTaskExecutorRule();
    private BorderWaitViewModel viewModel;
    private BorderWaitRepository repository;

    @Mock
    private Observer<List<BorderWaitEntity>> result;

    @Captor
    private ArgumentCaptor<MutableLiveData<ResourceStatus>> status;

    @Before
    public void init() {
        repository = mock(BorderWaitRepository.class);
        viewModel = new BorderWaitViewModel(repository);
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void empty() {
        viewModel.getBorderWaits().observeForever(result);
        verifyNoMoreInteractions(repository);
    }

    @Test
    public void basic(){
        ArgumentCaptor<String> direction = ArgumentCaptor.forClass(String.class);
        viewModel.init(BorderWaitViewModel.BorderDirection.NORTHBOUND);
        viewModel.getBorderWaits().observeForever(result);
        verify(repository, times(1)).getBorderWaitsFor(direction.capture(), status.capture());
        assert direction.getValue().equals("northbound");
        assert status.getValue().getValue().equals(ResourceStatus.loading());
    }

    @Test
    public void initSouthbound() {
        viewModel.init(BorderWaitViewModel.BorderDirection.SOUTHBOUND);
        verifyNoMoreInteractions(repository);
        assert(viewModel.getDirection().getValue() == BorderWaitViewModel.BorderDirection.SOUTHBOUND);
    }

    @Test
    public void initNorthbound() {
        viewModel.init(BorderWaitViewModel.BorderDirection.NORTHBOUND);
        verifyNoMoreInteractions(repository);
        assert(viewModel.getDirection().getValue() == BorderWaitViewModel.BorderDirection.NORTHBOUND);
    }

    @Test
    public void forceRefreshTriggersLoadingState() {
        ArgumentCaptor<Boolean> force = ArgumentCaptor.forClass(Boolean.class);
        viewModel.init(BorderWaitViewModel.BorderDirection.NORTHBOUND);
        viewModel.forceRefreshBorderWaits();
        verify(repository, times(1)).refreshData(status.capture(), force.capture());
        assert force.getValue();
        assert status.getValue().getValue().equals(ResourceStatus.loading());
    }
}