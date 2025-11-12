\documentclass[11pt,a4paper,sans]{moderncv}

% Modern CV style and color
\moderncvstyle{classic}
\moderncvcolor{blue}

% Character encoding
\usepackage[utf8]{inputenc}

% Adjust page margins - adaptive based on content
@if(hasProjects)@
\usepackage[scale=0.80]{geometry} % More content, tighter margins
@else@
\usepackage[scale=0.85]{geometry} % Standard margins
@endif@

% Conditional logic for adaptive layout
\usepackage{ifthen}
\usepackage{etoolbox}

% Personal information
\name{@basics.fullName@}{}
\title{@if(basics.label)@@basics.label@@endif@}
@if(basics.email)@\email{@basics.email@}@endif@
@if(basics.phone)@\phone{@basics.phone@}@endif@
@if(basics.url)@\homepage{@basics.url@}@endif@
@if(basics.location)@\address{@basics.location.city@}{@basics.location.countryCode@}{}@endif@

\begin{document}

\makecvtitle

% Summary (always first if present)
@if(basics.summary)@
\section{Summary}
@basics.summary@
@endif@

% Adaptive section ordering: Work experience before education for experienced professionals
@if(work)@
\section{Work Experience}
@work:{experience|
\cventry{@experience.startDate@ -- @if(experience.endDate)@@experience.endDate@@else@Present@endif@}{@experience.position@}{@experience.company@}{@if(experience.location)@@experience.location@@endif@}{}{
@if(experience.summary)@@experience.summary@\\@endif@
@if(experience.highlights)@
\begin{itemize}
@experience.highlights:{highlight|
\item @highlight@
}@
\end{itemize}
@endif@
}
}@
@endif@

% Education
@if(education)@
\section{Education}
@education:{edu|
\cventry{@edu.startDate@ -- @if(edu.endDate)@@edu.endDate@@else@Present@endif@}{@edu.studyType@ in @edu.area@}{@edu.institution@}{@if(edu.score)@GPA: @edu.score@@endif@}{}{}
}@
@endif@

% Skills
@if(skills)@
\section{Skills}
@skills:{category|
\cvitem{@category.name@}{@category.keywords; separator=", "@}
}@
@endif@

% Languages
@if(languages)@
\section{Languages}
@languages:{lang|
\cvitem{@lang.language@}{@lang.fluency@}
}@
@endif@

% Projects
@if(projects)@
\section{Projects}
@projects:{project|
\cventry{@if(project.startDate)@@project.startDate@ -- @if(project.endDate)@@project.endDate@@else@Present@endif@@endif@}{@project.name@}{@if(project.entity)@@project.entity@@endif@}{@if(project.url)@@project.url@@endif@}{}{
@if(project.description)@@project.description@\\@endif@
@if(project.highlights)@
\begin{itemize}
@project.highlights:{highlight|
\item @highlight@
}@
\end{itemize}
@endif@
}
}@
@endif@

\end{document}
