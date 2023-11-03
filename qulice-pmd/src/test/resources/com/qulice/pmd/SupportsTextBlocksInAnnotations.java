package foo;

@Operation(
        summary = "Get repository settings by name",
        description = """
            java 
            multiline
            text
            block
          """,
        responses = {
            @ApiResponse(
                description = "Returns repository setting json",
                responseCode = "200",
                content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(responseCode = "404", description = "Repository not found")
        }
    )
public cass SupportsTextBlocksInAnnotations {
    

