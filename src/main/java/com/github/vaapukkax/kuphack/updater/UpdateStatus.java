package com.github.vaapukkax.kuphack.updater;

public record UpdateStatus(GithubRelease release, String additional, boolean open) {}