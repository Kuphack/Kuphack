package dev.watukas.kuphack.updater;

import net.minecraft.text.Text;

public record UpdateStatus(GithubRelease release, Text additional, boolean open) {}