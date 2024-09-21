package io.github.wasabithumb.jdnsbench.tui.stage.impl.config;

import io.github.wasabithumb.jdnsbench.api.address.Address;
import io.github.wasabithumb.jdnsbench.api.address.provider.AddressProvider;
import io.github.wasabithumb.jdnsbench.tui.TUI;
import io.github.wasabithumb.jdnsbench.tui.stage.impl.ConfigStage;
import io.github.wasabithumb.jdnsbench.util.IPUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class AddIPAddressModal extends TextInputModal {

    private boolean cancelled = false;
    public AddIPAddressModal() {
        super("IP ADDRESS", "");
    }

    public @Nullable Address getAddress() {
        return this.cancelled ? null : Address.of(this.value);
    }

    @Override
    public int getMaxLength() {
        return 39;
    }

    @Override
    public boolean canSubmit() {
        return IPUtil.isValidV4(this.value) || IPUtil.isValidV6(this.value);
    }

    @Override
    protected boolean isValidChar(char c) {
        return ('0' <= c && c <= '9') || (c == '.') || (c == ':');
    }

    @Override
    protected void onCancel(@NotNull TUI tui, @NotNull ConfigStage stage) {
        this.cancelled = true;
    }

    @Override
    public @Nullable ConfigStageModal getNext() {
        final Address address = this.getAddress();
        if (address == null) return null;
        return new Secondary(address);
    }

    //

    static class Secondary extends TextInputModal {

        private final Address address;
        public Secondary(Address address) {
            super("LABEL FOR " + address.address(), "");
            this.address = address;
        }

        @Override
        public int getMaxLength() {
            return 64;
        }

        @Override
        public boolean canSubmit() {
            return true;
        }

        @Override
        protected void onSubmit(@NotNull TUI tui, @NotNull ConfigStage stage) {
            Address addr = this.address;
            if (!this.value.isEmpty()) addr = addr.label(this.value.toString());
            stage.addresses.add(AddressProvider.of(addr));
        }

    }

}
