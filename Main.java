import java.util.Random;

class Account {
    private double balance; // Баланс счета

    // Метод для получения текущего баланса
    public synchronized double getBalance() {
        return balance;
    }

    // Метод для пополнения счета
    public synchronized void deposit(double amount) {
        if (amount > 0) {
            balance += amount; // Увеличиваем баланс
            System.out.printf("Пополнено: %.2f, Новый баланс: %.2f%n", amount, balance);
            notifyAll(); // Уведомляем все ожидающие потоки
        }
    }

    // Метод для снятия средств со счета
    public synchronized void withdraw(double amount) throws InterruptedException {
        // Если средств недостаточно, ждем
        while (balance < amount) {
            System.out.printf("Ожидание достаточного баланса для снятия: %.2f. Текущий баланс: %.2f%n", amount, balance);
            wait(); // Ждем, пока баланс не станет достаточным
        }
        balance -= amount; // Уменьшаем баланс
        System.out.printf("Снято: %.2f, Новый баланс: %.2f%n", amount, balance);
    }
}

public class Main {
    private static final double AMOUNT_TO_WITHDRAW = 100.0; // Сумма для снятия
    private static final int DEPOSIT_INTERVAL_MS = 1000; // Интервал пополнения в миллисекундах

    public static void main(String[] args) {
        Account account = new Account(); // Создаем объект счета
        Random random = new Random(); // Создаем генератор случайных чисел

        // Поток для пополнения счета
        Thread depositThread = new Thread(() -> {
            try {
                while (true) {
                    double amount = random.nextDouble() * 50; // Случайная сумма для пополнения
                    account.deposit(amount);
                    Thread.sleep(DEPOSIT_INTERVAL_MS); // Ждем перед следующим пополнением
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // В случае прерывания возвращаемся к прерванному состоянию
            }
        });

        // Поток для снятия средств
        Thread withdrawThread = new Thread(() -> {
            try {
                // Ждем 5 секунд перед началом снятия средств
                Thread.sleep(5000);
                account.withdraw(AMOUNT_TO_WITHDRAW); // Снимаем сумму со счета
                System.out.printf("Итоговый баланс после снятия: %.2f%n", account.getBalance());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // В случае прерывания возвращаемся к прерванному состоянию
            }
        });

        depositThread.start(); // Запуск потока пополнения
        withdrawThread.start(); // Запуск потока снятия средств

        // Ждем завершения обоих потоков
        try {
            depositThread.join();
            withdrawThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // В случае прерывания возвращаемся к прерванному состоянию
        }
    }
}

