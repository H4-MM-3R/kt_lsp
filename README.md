Simple LSP server built using Kotlin, Gradle to get hands dirty with Language Server Protocol.

## Requirements

- JDK 11
- Gradle

## Setup

building and creating an executable
```zsh
gradle build installDist
```
executable location
```zsh
kt_lsp/app/build/install/app/bin/app
```

## Usage in Editors

example lets take the case of Neovim

(in your `~/.config/nvim/after/plugin/` directory)
create a lua file with the following content

```lua
local client = nil

vim.api.nvim_create_autocmd("FileType", {
	pattern = "text",
	callback = function()
		local bufnr = vim.api.nvim_get_current_buf()
		client = vim.lsp.start_client({
			name = "kt_lsp",
			cmd = { "<YOUR_EXECUTABLE_LOCATION_AFTER_BUILDING>" },
			capabilities = vim.lsp.protocol.make_client_capabilities(),
		})

		if not client then
			vim.notify("something went wrong with kt_lsp")
			return
		end
		vim.lsp.buf_attach_client(bufnr, client)
	end,
})
```
to attach the server to a buffer with `.txt` extension
