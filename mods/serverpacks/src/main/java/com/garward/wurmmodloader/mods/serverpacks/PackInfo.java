package com.garward.wurmmodloader.mods.serverpacks;

import java.nio.file.Path;

import com.garward.wurmmodloader.mods.serverpacks.api.ServerPacks;

class PackInfo {

	/**
	 * Path to the server pack.
	 */
	final Path path;

	/**
	 * in-memory pack data
	 */
	final byte[] data;

	/**
	 * Prepend the pack to the pack list on the client.
	 */
	final boolean prepend;

	/**
	 * Force the download
	 */
	final boolean force;

	/**
	 * SHA-256 hex digest of the pack bytes. Lazily filled by ServerPackMod
	 * at add-time. Sent to canonical clients so they can delta-skip when
	 * their cached copy already matches.
	 */
	String sha256;

	/**
	 * Size in bytes of the pack payload. Filled alongside {@link #sha256}.
	 * Sent to canonical clients for download progress UI.
	 */
	long size;

	/**
	 * Create a pack info
	 * @param path Path to the pack
	 * @param options Options
	 */
	public PackInfo(Path path, ServerPacks.ServerPackOptions... options) {
		this.path = path;
		this.data = null;
		this.prepend = ServerPacks.ServerPackOptions.PREPEND.isIn(options);
		this.force = ServerPacks.ServerPackOptions.FORCE.isIn(options);
	}

	/**
	 * Create a pack info for in-memory data
	 * @param data in-memory data
	 * @param options Options
	 */
	public PackInfo(byte[] data, ServerPacks.ServerPackOptions... options) {
		this.path = null;
		this.data = data;
		this.prepend = ServerPacks.ServerPackOptions.PREPEND.isIn(options);
		this.force = ServerPacks.ServerPackOptions.FORCE.isIn(options);
	}
}
