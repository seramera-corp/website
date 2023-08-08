<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Awesome Pattern Page!</title>
    <link rel="stylesheet" href="/static/main.css">
</head>
<body>
<h1>Pattern Details</h1>
<h2>This page should display details of a pattern</h2>
<div class="pattern-container">
    <div class="pattern">
        <a href="" class="pattern-name">
            ${pattern.name}
        </a>
        <a href="" class="pattern-publisher">
            ${pattern.publisher}
        </a>
    </div>
</div>
<h2>Recently finished projects using this pattern</h2>
<div class="pattern-projects">
    <#list projects as project>
        <div class="project">
            <img src="${project.imgUrl}" alt="" class="project-img">
            <a href="/project/${project.id}" class="project-name">
                ${project.name}
            </a>
            <a href="" class="project-user">
                ${project.user}
            </a>
        </div>
    <#else>
        No projects found for this pattern yet. Be the first one to start!
    </#list>
</div>
</body>
</html>
