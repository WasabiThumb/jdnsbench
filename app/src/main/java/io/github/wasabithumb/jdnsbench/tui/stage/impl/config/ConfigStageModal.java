package io.github.wasabithumb.jdnsbench.tui.stage.impl.config;

import io.github.wasabithumb.jdnsbench.tui.TUI;
import io.github.wasabithumb.jdnsbench.tui.TUIArrowKey;
import io.github.wasabithumb.jdnsbench.tui.stage.impl.ConfigStage;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
public interface ConfigStageModal {

    @ApiStatus.Internal
    static @NotNull ConfigStageModal addIPAddress() {
        return new AddIPAddressModal();
    }

    @ApiStatus.Internal
    static @NotNull ConfigStageModal addIPSource() {
        return new AddIPSourceModal();
    }

    @ApiStatus.Internal
    static @NotNull ConfigStageModal setDomain(@NotNull String initial) {
        return new SetDomainModal(initial);
    }

    @ApiStatus.Internal
    static @NotNull ConfigStageModal setRepeats(int initial) {
        return new SetRepeatsModal(initial);
    }

    @ApiStatus.Internal
    static @NotNull ConfigStageModal setTimeout(long timeout) {
        return new SetTimeoutModal(timeout);
    }

    @ApiStatus.Internal
    static @NotNull ConfigStageModal setDelay(long delay) {
        return new SetDelayModal(delay);
    }

    //

    void draw(@NotNull TUI tui, @NotNull ConfigStage stage, int cw, int ch);

    /** Returns true if the modal should close */
    boolean onInput(@NotNull TUI tui, @NotNull ConfigStage stage, char input);

    void onInputArrowKey(@NotNull TUI tui, @NotNull ConfigStage stage, @NotNull TUIArrowKey key);

    default @Nullable ConfigStageModal getNext() {
        return null;
    }

}
