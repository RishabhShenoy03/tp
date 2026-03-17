package duke;

import java.nio.file.Path;

public record ParsedCommand(
        CommandType type,
        String name,
        AssetType assetType,
        String ticker,
        Double quantity,
        Double price,
        String listTarget,
        Path filePath
) {
}
