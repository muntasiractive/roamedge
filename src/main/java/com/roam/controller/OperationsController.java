package com.roam.controller;

import com.roam.model.Operation;
import com.roam.model.Region;
import com.roam.service.OperationService;
import com.roam.service.OperationServiceImpl;
import com.roam.repository.RegionRepository;
import com.roam.util.DialogUtils;
import com.roam.view.components.OperationDialog;
import com.roam.view.components.OperationTableView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Controller responsible for managing operations list view functionality.
 * <p>
 * This controller handles all operations management including:
 * <ul>
 * <li>Operation CRUD operations (create, read, update, delete)</li>
 * <li>Operations table view data binding and refresh</li>
 * <li>Region integration for operation categorization</li>
 * <li>Data change notification handling</li>
 * </ul>
 * </p>
 *
 * @author Roam Development Team
 * @version 1.0
 * @since 1.0
 */
public class OperationsController {

    // ==================== Fields ====================

    private static final Logger logger = LoggerFactory.getLogger(OperationsController.class);
    private final OperationService operationService;
    private final RegionRepository regionRepository;
    private OperationTableView tableView;
    private Runnable onDataChanged;

    // ==================== Constructor ====================

    public OperationsController() {
        this.operationService = new OperationServiceImpl();
        this.regionRepository = new RegionRepository();
    }

    // ==================== Configuration Methods ====================

    public void setTableView(OperationTableView tableView) {
        this.tableView = tableView;
    }

    public void setOnDataChanged(Runnable handler) {
        this.onDataChanged = handler;
    }

    // ==================== CRUD Operations ====================

    /**
     * Load all operations from database
     */
    public List<Operation> loadOperations() {
        try {
            return operationService.findAll();
        } catch (Exception e) {
            logger.error("Failed to load operations: {}", e.getMessage(), e);
            DialogUtils.showError(
                    "Database Error",
                    "Failed to load operations",
                    e.getMessage());
            return List.of();
        }
    }

    /**
     * Create new operation
     */
    public void createOperation() {
        List<Region> regions = regionRepository.findAll();
        OperationDialog dialog = new OperationDialog(null, regions);
        Optional<Operation> result = dialog.showAndWait();

        result.ifPresent(operation -> {
            try {
                Operation savedOperation = operationService.createOperation(operation);
                logger.debug("✓ Operation created: {}", savedOperation.getName());
                refreshTable();
            } catch (Exception e) {
                logger.error("✗ Failed to create operation: {}", e.getMessage(), e);
                DialogUtils.showError(
                        "Save Error",
                        "Failed to create operation",
                        e.getMessage());
            }
        });
    }

    /**
     * Edit existing operation
     */
    public void editOperation(Operation operation) {
        if (operation == null)
            return;

        List<Region> regions = regionRepository.findAll();
        OperationDialog dialog = new OperationDialog(operation, regions);
        Optional<Operation> result = dialog.showAndWait();

        result.ifPresent(updatedOp -> {
            try {
                Operation savedOperation = operationService.updateOperation(updatedOp);
                logger.debug("✓ Operation updated: {}", savedOperation.getName());
                refreshTable();
            } catch (Exception e) {
                logger.error("✗ Failed to update operation: {}", e.getMessage(), e);
                DialogUtils.showError(
                        "Update Error",
                        "Failed to update operation",
                        e.getMessage());
            }
        });
    }

    /**
     * Delete operation with confirmation
     */
    public void deleteOperation(Operation operation) {
        if (operation == null)
            return;

        boolean confirmed = DialogUtils.showDeleteConfirmation(
                operation.getName(),
                "Operation",
                "All associated tasks and events will also be deleted.");

        if (confirmed) {
            try {
                operationService.deleteOperation(operation.getId());
                logger.debug("✓ Operation deleted: {}", operation.getName());
                refreshTable();
            } catch (Exception e) {
                logger.error("✗ Failed to delete operation: {}", e.getMessage(), e);
                DialogUtils.showError(
                        "Delete Error",
                        "Failed to delete operation",
                        e.getMessage());
            }
        }
    }

    /**
     * Refresh table data
     */
    public void refreshTable() {
        if (tableView != null) {
            List<Operation> operations = loadOperations();
            tableView.getItems().setAll(operations);
        }

        if (onDataChanged != null) {
            onDataChanged.run();
        }
    }
}
