package Algorithm;

import java.util.EmptyStackException;
import java.util.Stack;

public class MyStack<T> extends Stack<T> {

    // Method to pop multiple elements from the stack
    public void popMultiple(int numberOfElements) {
        if (numberOfElements < 0) {
            throw new IllegalArgumentException("Number of elements to pop cannot be negative");
        }
        for (int i = 0; i < numberOfElements; i++) {
            if (!this.isEmpty()) {
                this.pop();
            } else {
                System.out.println("The stack is empty. Cannot pop more elements.");
                break;
            }
        }
    }

    // Method to increment the top element if it's an integer
    public void incrementTopElement() {
        if (this.isEmpty()) {
            throw new EmptyStackException();
        }

        try {
            T topElement = this.pop();
            if (topElement instanceof Integer) {
                Integer incrementedValue = (Integer) topElement + 1;
                this.push((T) incrementedValue);
            } else {
                throw new IllegalArgumentException("Top element is not an integer");
            }
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Error in casting the top element", e);
        }
    }

    // Method to increment the top element if it's an integer
    public boolean decrementTopElement() {
        if (this.isEmpty()) {
            throw new EmptyStackException();
        }

        try {
            T topElement = this.pop();
            if (topElement instanceof Integer) {
                Integer decrementedValue = (Integer) topElement;
                if(decrementedValue == 0) return false;
                decrementedValue--;
                this.push((T) decrementedValue);
            } else {
                throw new IllegalArgumentException("Top element is not an integer");
            }
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Error in casting the top element", e);
        }
    return true;
    }

}
