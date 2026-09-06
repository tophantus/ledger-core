interface LogoProps {
    size?: number;
}

export function Logo({
                         size = 40,
                     }: LogoProps) {
    return (
        <svg
            xmlns="http://www.w3.org/2000/svg"
            width={size}
            height={size}
            viewBox="0 0 1254 1254"
            fill="none"
            aria-label="LedgerCore"
            role="img"
        >
            <defs>
                <linearGradient
                    id="navy"
                    x1="300"
                    y1="150"
                    x2="1060"
                    y2="1060"
                    gradientUnits="userSpaceOnUse"
                >
                    <stop offset="0" stopColor="#062D78" />
                    <stop
                        offset="0.52"
                        stopColor="#03245F"
                    />
                    <stop
                        offset="1"
                        stopColor="#001B4D"
                    />
                </linearGradient>

                <linearGradient
                    id="electricBlue"
                    x1="470"
                    y1="780"
                    x2="930"
                    y2="490"
                    gradientUnits="userSpaceOnUse"
                >
                    <stop
                        offset="0"
                        stopColor="#0A6BFF"
                    />
                    <stop
                        offset="0.55"
                        stopColor="#0860F2"
                    />
                    <stop
                        offset="1"
                        stopColor="#0757E6"
                    />
                </linearGradient>
            </defs>

            <path
                d="M 543 1011 L 650 1073 L 663 1077 L 682 1076 L 690 1073 L 1046 877 L 1060 864 L 1067 848 L 1067 701 L 1063 690 L 1053 678 L 994 644 L 955 624 L 937 619 L 917 628 L 792 698 L 786 700 L 774 700 L 764 696 L 625 618 L 625 711 L 643 722 L 765 787 L 779 789 L 819 768 L 939 699 L 940 788 L 797 871 Z M 631 570 L 631 578 L 637 585 L 759 654 L 773 659 L 788 659 L 800 655 L 923 586 L 930 580 L 932 574 L 930 567 L 926 563 L 800 493 L 787 489 L 774 489 L 754 497 L 638 562 Z M 267 281 L 267 847 L 273 864 L 282 875 L 363 923 L 363 358 L 358 344 L 349 334 L 288 292 Z M 1066 421 L 1064 414 L 1058 404 L 1048 396 L 817 265 L 795 257 L 773 257 L 758 262 L 541 388 L 515 405 L 507 417 L 504 428 L 504 540 L 527 529 L 755 404 L 776 396 L 795 398 L 810 405 L 1028 529 L 1045 533 L 1056 529 L 1062 523 L 1066 514 Z M 473 131 L 325 213 L 312 230 L 309 241 L 309 250 L 313 263 L 324 276 L 356 297 L 375 313 L 384 325 L 391 345 L 391 921 L 539 1008 L 660 940 L 718 902 L 501 776 L 475 759 L 475 262 Z"
                fill="url(#navy)"
                fillRule="evenodd"
            />

            <path
                d="M 546 1009 L 543 1011 L 545 1012 L 544 1010 Z M 391 759 L 391 921 L 539 1008 L 629 958 L 681 927 L 672 920 L 395 760 Z M 631 570 L 631 578 L 637 585 L 717 629 L 759 654 L 773 659 L 788 659 L 800 655 L 923 586 L 930 580 L 932 574 L 930 567 L 926 563 L 800 493 L 787 489 L 774 489 L 766 491 L 638 562 L 633 566 Z"
                fill="url(#electricBlue)"
                fillRule="evenodd"
            />
        </svg>
    );
}