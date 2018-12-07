package gov.wa.wsdot.android.wsdot.ui.ferries;

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

import gov.wa.wsdot.android.wsdot.database.ferries.FerryScheduleEntity;
import gov.wa.wsdot.android.wsdot.repository.FerryScheduleRepository;
import gov.wa.wsdot.android.wsdot.util.threading.AppExecutors;
import gov.wa.wsdot.android.wsdot.util.network.ResourceStatus;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(MockitoJUnitRunner.class)
public class FerrySchedulesViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantExecutor = new InstantTaskExecutorRule();
    private FerrySchedulesViewModel viewModel;
    private FerryScheduleRepository repository;
    private AppExecutors appExecutors;

    @Mock
    private Observer<List<FerryScheduleEntity>> results;

    @Mock
    private Observer<FerryScheduleEntity> result;

    @Captor
    private ArgumentCaptor<MutableLiveData<ResourceStatus>> status;

    @Captor
    private ArgumentCaptor<Integer> id;

    @Captor
    private ArgumentCaptor<Boolean> force;

    @Before
    public void init() {
        repository = mock(FerryScheduleRepository.class);
        appExecutors = mock(AppExecutors.class);
        viewModel = new FerrySchedulesViewModel(repository, appExecutors);
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void initWithAllSchedules() {
        viewModel.init(null);
        verify(repository, times(1)).loadFerrySchedules(status.capture());
    }

    @Test
    public void initWithOneSchedule() {
        viewModel.init(1);
        verify(repository, times(1)).loadFerryScheduleFor(id.capture(), status.capture());
        assert id.getValue() == 1;
    }

    @Test
    public void basicSchedules(){
        viewModel.init(null);
        verify(repository, times(1)).loadFerrySchedules(status.capture());
        viewModel.getFerrySchedules().observeForever(results);
        verifyNoMoreInteractions(repository);
        assert status.getValue().getValue().equals(ResourceStatus.loading());
    }

    @Test
    public void basicSchedule(){
        viewModel.init(1);
        verify(repository, times(1)).loadFerryScheduleFor(id.capture(), status.capture());
        viewModel.getFerrySchedule().observeForever(result);
        verifyNoMoreInteractions(repository);
        assert status.getValue().getValue().equals(ResourceStatus.loading());
        assert id.getValue().equals(1);
    }

    @Test
    public void forceRefreshTriggersLoadingState() {
        viewModel.init(null);
        viewModel.forceRefreshFerrySchedules();
        verify(repository, times(1)).refreshData(status.capture(), force.capture());
        assert force.getValue();
        assert status.getValue().getValue().equals(ResourceStatus.loading());
    }

}
