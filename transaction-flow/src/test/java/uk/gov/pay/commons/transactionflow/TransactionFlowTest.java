package uk.gov.pay.commons.transactionflow;

import org.junit.Test;
import uk.gov.pay.commons.transactionflow.operations.NonTransactionalOperation;
import uk.gov.pay.commons.transactionflow.operations.PreTransactionalOperation;
import uk.gov.pay.commons.transactionflow.operations.TransactionalOperation;

import javax.persistence.OptimisticLockException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

public class TransactionFlowTest {

    @Test
    public void shouldExecuteANonTransactionalOperationAndPreserveResultInContext() throws Exception {
        TransactionContext mockContext = mock(TransactionContext.class);
        TransactionFlow flow = new TransactionFlow(mockContext);
        flow.executeNext((NonTransactionalOperation<TransactionContext, String>) ctx -> "Foo");

        verify(mockContext, times(1)).put("Foo");
        assertThat(flow.complete(), is(mockContext));
    }

    @Test
    public void shouldExecuteATransactionalOperationAndPreserveResultInContext() throws Exception {
        TransactionContext mockContext = mock(TransactionContext.class);
        TransactionFlow flow = new TransactionFlow(mockContext);
        flow.executeNext((TransactionalOperation<TransactionContext, String>) ctx -> "Bar");
        verify(mockContext, times(1)).put("Bar");

        assertThat(flow.complete(), is(mockContext));
    }

    @Test
    public void shouldExecuteAPreTransactionalOperationAndPreserveResultInContext() throws Exception {
        TransactionContext mockContext = mock(TransactionContext.class);
        TransactionFlow flow = new TransactionFlow(mockContext);
        flow.executeNext((PreTransactionalOperation<TransactionContext, String>) ctx -> "Baz");

        verify(mockContext, times(1)).put("Baz");
        assertThat(flow.complete(), is(mockContext));
    }

    @Test
    public void shouldNotPreserveResultInContextIfResultValueIsNull() throws Exception {
        TransactionContext mockContext = mock(TransactionContext.class);
        TransactionFlow flow = new TransactionFlow(mockContext);
        flow.executeNext((PreTransactionalOperation<TransactionContext, String>) ctx -> null);

        verify(mockContext, times(0)).put(any());
        assertThat(flow.complete(), is(mockContext));
    }

    @Test(expected = TransactionalException.class)
    public void shouldReportConflictsIfDatabaseOperationIsInVersionConflict() throws Exception {
        TransactionContext mockContext = mock(TransactionContext.class);
        TransactionFlow flow = new TransactionFlow(mockContext);
        flow.executeNext((PreTransactionalOperation<TransactionContext, String>) ctx -> {
            if (true) { //to fool compiler and to always throw an exception
                throw new OptimisticLockException("I'm in conflict");
            } else {
                return "should not return";
            }
        });
    }

    @Test(expected = NullPointerException.class)
    public void shouldErrorIfProvidedOperationIsNull_forTransactionalOperation() throws Exception {
        TransactionContext mockContext = mock(TransactionContext.class);
        TransactionFlow flow = new TransactionFlow(mockContext);
        flow.executeNext((TransactionalOperation<TransactionContext, String>) null);
    }

    @Test(expected = NullPointerException.class)
    public void shouldErrorIfProvidedOperationIsNull_forNonTransactionalOperation() throws Exception {
        TransactionContext mockContext = mock(TransactionContext.class);
        TransactionFlow flow = new TransactionFlow(mockContext);
        flow.executeNext((NonTransactionalOperation<TransactionContext, String>) null);
    }


}
