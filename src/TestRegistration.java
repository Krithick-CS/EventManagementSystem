public class TestRegistration {
    public static void main(String[] args) {
        RegistrationDAO dao = new RegistrationDAO();

        System.out.println("1. Arun registers (expect SUCCESS):");
        dao.register(3, 1);

        System.out.println("2. Arun again (expect DUPLICATE rejection):");
        dao.register(3, 1);

        System.out.println("3. Priya registers (expect SUCCESS):");
        dao.register(3, 2);

        System.out.println("4. Kavya registers, event now full (expect FULL rejection):");
        dao.register(3, 3);

        System.out.println("5. Nonexistent event (expect NOT FOUND):");
        dao.register(99, 1);

        System.out.println("Total registered for event 3: " + dao.countRegistrations(3));
    }
}