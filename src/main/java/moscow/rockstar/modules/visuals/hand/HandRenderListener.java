package moscow.rockstar.modules.visuals.hand;

import moscow.rockstar.events.EventListener;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;
import pyrock.events.render.HandRenderEvent;

class HandRenderListener implements EventListener<HandRenderEvent> {
    private final ViewModel viewModel;

    HandRenderListener(ViewModel viewModel) {
        this.viewModel = viewModel;
    }

    public void onHandRender(HandRenderEvent event) {
        MatrixStack matrices = event.getMatrices();
        boolean mainHand = event.getArm() == Arm.RIGHT;
        float offSize = viewModel.sizeLeft.getValue() - 1.0f;
        float mainSize = viewModel.sizeRight.getValue() - 1.0f;
        if (mainHand) {
            matrices.translate(viewModel.mainTranslateX.getX() - mainSize,
                -viewModel.mainTranslateX.getY() + mainSize / 2.0f, mainSize);
        } else {
            matrices.translate(viewModel.offTranslateX.getX() + offSize,
                -viewModel.offTranslateX.getY() + offSize / 2.0f, offSize);
        }
        if (viewModel.isChatEditReady()) {
            viewModel.onHandRender(event);
        } else {
            viewModel.editingHand = null;
        }
    }

    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    public void onEvent(HandRenderEvent event) {
        onHandRender(event);
    }
}
