package com.example.boundary;

import com.example.entity.LoanOffer;
import io.quarkus.mongodb.panache.reactive.ReactivePanacheQuery;
import io.quarkus.panache.common.Sort;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@QuarkusTest
class LoanOfferRepositoryTest {
    @InjectMock
    LoanOfferRepository loanOfferRepository;

    @ConfigProperty(name = "mongo.pagesize")
    int pageSize;

    private static Logger logger = LoggerFactory.getLogger(LoanOfferRepositoryTest.class);

    ReactivePanacheQuery<LoanOffer> mockOffers = mock(ReactivePanacheQuery.class);
    ReactivePanacheQuery<LoanOffer> mockPage0 = mock(ReactivePanacheQuery.class);
    ReactivePanacheQuery<LoanOffer> mockPage1 = mock(ReactivePanacheQuery.class);
    ReactivePanacheQuery<LoanOffer> mockPage2 = mock(ReactivePanacheQuery.class);

    @BeforeEach
    public void setupMocks() {
        when(mockOffers.page(eq(0), eq(pageSize))).thenReturn(mockPage0);
        when(mockOffers.page(eq(1), eq(pageSize))).thenReturn(mockPage1);
        when(mockOffers.page(eq(2), eq(pageSize))).thenReturn(mockPage2);
    }

    /** A lot of mocking required in this method because we do paging on the repository
     *
     * @throws Throwable
     */
    @Test
    public void givenLoanOffersForValueFitInSinglePageThenTwoCallsToDbMade() {
        BigDecimal amount = new BigDecimal(2000);
        List<LoanOffer> listOfLoans = getListOfLoanOffers("2000", "0.07", "test", pageSize);
        Uni<List<LoanOffer>> fakeUnilistOfLoans = Uni
                .createFrom()
                .item(listOfLoans);
        Uni<List<LoanOffer>> emptyUniListOfLoans = Uni.createFrom().item(Collections.emptyList());

        when(mockPage0.list()).thenReturn(fakeUnilistOfLoans);
        when(mockPage1.list()).thenReturn(emptyUniListOfLoans);
        when(loanOfferRepository.findAll(any())).thenReturn(mockOffers);

        when(loanOfferRepository.retrieveLoanOffersThatSumToAtLeastValue(amount)).thenCallRealMethod();
        Uni<List<LoanOffer>> listUniOfLoanOffers = loanOfferRepository.retrieveLoanOffersThatSumToAtLeastValue(amount);

        // get the result
        List<LoanOffer> listOfLoanOffers = listUniOfLoanOffers.subscribe().withSubscriber(UniAssertSubscriber.create()).getItem();
        List<LoanOffer> expectedListOfLoans = listOfLoans;
        expectedListOfLoans.addAll(listOfLoans);
        assertThat(listOfLoanOffers).contains(expectedListOfLoans.toArray(new LoanOffer[2]));

        // verify and assert that methods have been called with certain args
        ArgumentCaptor<Integer> pageIndex = ArgumentCaptor.forClass(Integer.class);
        verify(mockOffers, times(2)).page(pageIndex.capture(), eq(pageSize));
        List<Integer> allPageIndexValues = pageIndex.getAllValues();
        assertThat(allPageIndexValues.get(0)).isEqualTo(0);
        assertThat(allPageIndexValues.get(1)).isEqualTo(1);
    }

    @Test
    public void givenLoanOffersForValueFitInTwoPagesThenTwoCallsToDbMade() {
        BigDecimal amount = new BigDecimal(8000);
        List<LoanOffer> listOfLoans = getListOfLoanOffers("2000", "0.07", "test", pageSize);
        Uni<List<LoanOffer>> fakeUnilistOfLoans = Uni
                .createFrom()
                .item(listOfLoans);
        Uni<List<LoanOffer>> emptyUniListOfLoans = Uni.createFrom().item(Collections.emptyList());

        when(mockPage0.list()).thenReturn(fakeUnilistOfLoans);
        when(mockPage1.list()).thenReturn(fakeUnilistOfLoans);
        when(mockPage2.list()).thenReturn(emptyUniListOfLoans);
        when(loanOfferRepository.findAll(any())).thenReturn(mockOffers);

        when(loanOfferRepository.retrieveLoanOffersThatSumToAtLeastValue(amount)).thenCallRealMethod();
        Uni<List<LoanOffer>> listUniOfLoanOffers = loanOfferRepository.retrieveLoanOffersThatSumToAtLeastValue(amount);

        // get the result
        List<LoanOffer> listOfLoanOffers = listUniOfLoanOffers.subscribe().withSubscriber(UniAssertSubscriber.create()).getItem();
        List<LoanOffer> expectedListOfLoans = listOfLoans;
        expectedListOfLoans.addAll(listOfLoans);
        assertThat(listOfLoanOffers).contains(expectedListOfLoans.toArray(new LoanOffer[4]));

        // verify and assert that methods have been called with certain args
        ArgumentCaptor<Integer> pageIndex = ArgumentCaptor.forClass(Integer.class);
        verify(mockOffers, times(3)).page(pageIndex.capture(), eq(pageSize));
        List<Integer> allPageIndexValues = pageIndex.getAllValues();
        assertThat(allPageIndexValues.get(0)).isEqualTo(0);
        assertThat(allPageIndexValues.get(1)).isEqualTo(1);
        assertThat(allPageIndexValues.get(2)).isEqualTo(2);
    }

    @Test
    public void givenNoLoanOffersCanMatchThenResultShouldBeNull() {
        BigDecimal amount = new BigDecimal(10000);
        List<LoanOffer> listOfLoans = getListOfLoanOffers("2000", "0.07", "test", pageSize);
        Uni<List<LoanOffer>> fakeUnilistOfLoans = Uni
                .createFrom()
                .item(listOfLoans);
        Uni<List<LoanOffer>> emptyUniListOfLoans = Uni.createFrom().item(Collections.emptyList());

        when(mockPage0.list()).thenReturn(fakeUnilistOfLoans);
        when(mockPage1.list()).thenReturn(emptyUniListOfLoans);
        when(loanOfferRepository.findAll(any())).thenReturn(mockOffers);

        when(loanOfferRepository.retrieveLoanOffersThatSumToAtLeastValue(amount)).thenCallRealMethod();
        Uni<List<LoanOffer>> listUniOfLoanOffers = loanOfferRepository.retrieveLoanOffersThatSumToAtLeastValue(amount);

        List<LoanOffer> listOfLoanOffers = listUniOfLoanOffers.subscribe().withSubscriber(UniAssertSubscriber.create()).getItem();
        assertThat(listOfLoanOffers).isNullOrEmpty();

        // verify and assert that methods have been called with certain args
        ArgumentCaptor<Integer> pageIndex = ArgumentCaptor.forClass(Integer.class);
        verify(mockOffers, times(2)).page(pageIndex.capture(), eq(pageSize));
        List<Integer> allPageIndexValues = pageIndex.getAllValues();
        assertThat(allPageIndexValues.get(0)).isEqualTo(0);
        assertThat(allPageIndexValues.get(1)).isEqualTo(1);
    }

    private List<LoanOffer> getListOfLoanOffers(String amount, String rate, String lenderIdPrefix, int numberOfLoanOffers) {
        AtomicInteger idPostfix = new AtomicInteger(0);
        return Stream.generate(() ->
                new LoanOffer(amount, rate, lenderIdPrefix + idPostfix.getAndIncrement()))
                .limit(numberOfLoanOffers)
                .collect(Collectors.toList());
    }
}