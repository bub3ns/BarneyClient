package moscow.rockstar.ui.hud;

import java.util.Stack;

/** Undo/redo history for HUD element drags. 1:1 with rockstar/ilIlil/IiIiIIiiI. */
public class HudMoveHistory {
    private final Stack<Move> undoStack = new Stack<Move>();
    private final Stack<Move> redoStack = new Stack<Move>();

    public void record(HudElement element, float fromX, float fromY, float toX, float toY) {
        this.undoStack.push(new Move(element, fromX, fromY, toX, toY, System.currentTimeMillis()));
        this.redoStack.clear();
    }

    public void undo() {
        if (this.undoStack.isEmpty()) {
            return;
        }
        Move move = this.undoStack.pop();
        move.element().pos(move.fromX(), move.fromY());
        this.redoStack.push(move);
    }

    public void redo() {
        if (this.redoStack.isEmpty()) {
            return;
        }
        Move move = this.redoStack.pop();
        move.element().pos(move.toX(), move.toY());
        this.undoStack.push(move);
    }

    public boolean canUndo() {
        return !this.undoStack.isEmpty();
    }

    public boolean canRedo() {
        return !this.redoStack.isEmpty();
    }

    public long lastUndoTime() {
        return this.undoStack.isEmpty() ? Long.MIN_VALUE : this.undoStack.peek().time();
    }

    public long lastRedoTime() {
        return this.redoStack.isEmpty() ? Long.MIN_VALUE : this.redoStack.peek().time();
    }

    record Move(HudElement element, float fromX, float fromY, float toX, float toY, long time) {
    }
}
