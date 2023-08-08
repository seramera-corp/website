<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Static page</title>
    <link rel="stylesheet" href="/static/main.css">
</head>
<body>
<h1>Recent Projects:</h1>
<section class="projects">
    <#list projects as project>
    <div class="project">
        <img src="${project.imgUrl}" alt="" class="project-img">
        <a href="/project/${project.id}" class="project-name">
            ${project.name}
        </a>
        <a href="" class="project-user">
            ${project.user}
        </a>
        <a href="/pattern/${project.patternId}" class="project-pattern">
            ${project.pattern}
        </a>
    </div>
    </#list>
</section>
</body>
</html>
