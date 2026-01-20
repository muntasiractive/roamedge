package com.roam.test;

import com.roam.model.Operation;
import com.roam.model.OperationStatus;
import com.roam.model.Priority;
import com.roam.repository.OperationRepository;
import com.roam.service.DatabaseService;
import com.roam.util.HibernateUtil;

import java.time.LocalDate;
import java.util.List;

public class DatabaseTest {

    public static void main(String[] args) {
        try {
            // Initialize database
            DatabaseService.initializeDatabase();

            // Create repository
            OperationRepository repo = new OperationRepository();

            // Test 1: Create operations
            System.out.println("\n📝 Test 1: Creating operations...");
            Operation op1 = new Operation("Learn JavaFX");
            op1.setPurpose("Master JavaFX for desktop development");
            op1.setPriority(Priority.HIGH);
            op1.setStatus(OperationStatus.IN_PROGRESS);
            op1.setDueDate(LocalDate.now().plusDays(30));
            repo.save(op1);

            Operation op2 = new Operation("Build Roam App");
            op2.setPurpose("Create a personal knowledge management system");
            op2.setPriority(Priority.HIGH);
            op2.setStatus(OperationStatus.ONGOING);
            repo.save(op2);

            Operation op3 = new Operation("Study Hibernate");
            op3.setPurpose("Learn JPA and Hibernate ORM");
            op3.setPriority(Priority.MEDIUM);
            op3.setStatus(OperationStatus.END);
            repo.save(op3);

            // Test 2: Count operations
            System.out.println("\n📊 Test 2: Counting operations...");
            long count = repo.count();
            System.out.println("Total operations: " + count);

            // Test 3: Find all
            System.out.println("\n📋 Test 3: Finding all operations...");
            List<Operation> allOps = repo.findAll();
            allOps.forEach(System.out::println);

            // Test 4: Find by ID
            System.out.println("\n🔍 Test 4: Finding by ID...");
            repo.findById(op1.getId()).ifPresent(op -> System.out.println("Found: " + op));

            // Test 5: Find by status
            System.out.println("\n🏷️ Test 5: Finding by status (IN_PROGRESS)...");
            List<Operation> inProgress = repo.findByStatus(OperationStatus.IN_PROGRESS);
            inProgress.forEach(System.out::println);

            // Test 6: Find by priority
            System.out.println("\n⚡ Test 6: Finding by priority (HIGH)...");
            List<Operation> highPriority = repo.findByPriority(Priority.HIGH);
            highPriority.forEach(System.out::println);

            // Test 7: Update operation
            System.out.println("\n✏️ Test 7: Updating operation...");
            op1.setOutcome("Successfully completed JavaFX basics");
            op1.setStatus(OperationStatus.END);
            repo.save(op1);

            // Test 8: Delete operation
            System.out.println("\n🗑️ Test 8: Deleting operation...");
            repo.delete(op3);
            System.out.println("Remaining operations: " + repo.count());

            System.out.println("\n✅ All tests completed successfully!");

        } catch (Exception e) {
            System.err.println("❌ Test failed: " + e.getMessage());
            e.printStackTrace();
        } finally {
            HibernateUtil.shutdown();
        }
    }
}
