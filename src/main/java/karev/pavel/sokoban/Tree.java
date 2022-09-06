package karev.pavel.sokoban;

public class Tree {

    Node root;

    public Tree(Level level) {
        root = new Node(new State(level));
    }

    public Tree(Node root) {
        this.root = root;
    }

    public Node getRoot() {
        return root;
    }

    public void setRoot(Node root) {
        this.root = root;
    }

}
