/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2013, Red Hat Inc. or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.hibernate.resource.transaction;

import org.hibernate.engine.transaction.spi.IsolationDelegate;
import org.hibernate.engine.transaction.spi.TransactionObserver;
import org.hibernate.resource.transaction.spi.TransactionStatus;

/**
 * Models the coordination of all transaction related flows.
 *
 * @author Steve Ebersole
 */
public interface TransactionCoordinator {
	/**
	 * Indicates an explicit request to join a transaction.  This is mainly intended to handle the JPA requirement
	 * around {@link javax.persistence.EntityManager#joinTransaction()}, and generally speaking only has an impact in
	 * JTA environments
	 */
	public void explicitJoin();

	/**
	 * Determine if there is an active transaction that this coordinator is already joined to.
	 *
	 * @return {@code true} if there is an active transaction this coordinator is already joined to; {@code false}
	 * otherwise.
	 */
	public boolean isJoined();

	/**
	 * Used by owner of the JdbcSession as a means to indicate that implicit joining should be done if needed.
	 */
	public void pulse();

	/**
	 * Get the delegate used by the local transaction driver to control the underlying transaction
	 *
	 * @return The control delegate.
	 */
	public TransactionDriver getTransactionDriverControl();

	/**
	 * Get access to the local registry of Synchronization instances
	 *
	 * @return The local Synchronization registry
	 */
	public SynchronizationRegistry getLocalSynchronizations();

	/**
	 * Is this transaction still active?
	 * <p/>
	 * Answers on a best effort basis.  For example, in the case of JDBC based transactions we cannot know that a
	 * transaction is active when it is initiated directly through the JDBC {@link java.sql.Connection}, only when
	 * it is initiated from here.
	 *
	 * @return {@code true} if the transaction is still active; {@code false} otherwise.
	 *
	 * @throws org.hibernate.HibernateException Indicates a problem checking the transaction status.
	 */
	public boolean isActive();

	/**
	 * Retrieve an isolation delegate appropriate for this transaction strategy.
	 *
	 * @return An isolation delegate.
	 */
	public IsolationDelegate createIsolationDelegate();

	/**
	 * Adds an observer to the coordinator.
	 * <p/>
	 * observers are not to be cleared on transaction completion.
	 *
	 * @param observer The observer to add.
	 */
	public void addObserver(TransactionObserver observer);

	/**
	 * Removed an observer from the coordinator.
	 *
	 * @param observer The observer to remove.
	 */
	public void removeObserver(TransactionObserver observer);

	/**
	 *
	 * @return
	 */
	public TransactionCoordinatorBuilder getTransactionCoordinatorBuilder();

	public void setTimeOut(int seconds);

	public int getTimeOut();

	/**
	 * Provides the means for "local transactions" (as transaction drivers) to control the
	 * underlying "physical transaction" currently associated with the TransactionCoordinator.
	 *
	 * @author Steve Ebersole
	 */
	public interface TransactionDriver {
		/**
		 * Begin the physical transaction
		 */
		public void begin();

		/**
		 * Commit the physical transaction
		 */
		public void commit();

		/**
		 * Rollback the physical transaction
		 */
		public void rollback();

		public TransactionStatus getStatus();

		public void markRollbackOnly();

		// todo : org.hibernate.Transaction will need access to register local Synchronizations.
		//		depending on how we integrate TransactionCoordinator/TransactionDriverControl with
		//		org.hibernate.Transaction that might be best done by:
		//			1) exposing registerSynchronization here (if the Transaction is just passed this)
		//			2) using the exposed TransactionCoordinator#getLocalSynchronizations (if the Transaction is passed the TransactionCoordinator)
		//
		//		if
	}
}
