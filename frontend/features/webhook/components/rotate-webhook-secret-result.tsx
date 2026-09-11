"use client";

import {
    Check,
    Copy,
    X,
} from "lucide-react";
import {
    useTranslations,
} from "next-intl";
import {
    useState,
} from "react";

import {Button} from "@/components/ui/button";

interface RotateWebhookSecretResultProps {
    secret: string;
    onClose: () => void;
}

export function RotateWebhookSecretResult({
                                              secret,
                                              onClose,
                                          }: RotateWebhookSecretResultProps) {
    const t =
        useTranslations("webhook");

    const [
        isCopied,
        setIsCopied,
    ] = useState(false);

    const handleCopy = async () => {
        try {
            await navigator.clipboard.writeText(
                secret,
            );

            setIsCopied(true);

            window.setTimeout(() => {
                setIsCopied(false);
            }, 2000);
        } catch {
            setIsCopied(false);
        }
    };

    return (
        <div
            className="
                fixed
                inset-0
                z-[60]
                flex
                items-center
                justify-center
                bg-black/40
                p-4
                text-left
            "
            onMouseDown={(
                event,
            ) => {
                if (
                    event.target ===
                    event.currentTarget
                ) {
                    onClose();
                }
            }}
        >
            <div className="
                w-full
                max-w-lg
                min-w-0
                overflow-hidden
                rounded-lg
                border
                border-border
                bg-surface
                text-left
                shadow-lg
            ">
                {/* Success header */}
                <div
                    className="
                        flex
                        items-start
                        justify-between
                        border-b
                        border-border
                        px-6
                        py-4
                        text-left
                    "
                >
                    <div className="
                        min-w-0
                        flex-1
                        text-left
                    ">
                        <h2 className="
                            text-left
                            text-lg
                            font-semibold
                            text-primary
                        ">
                            {t(
                                "rotateSecret.success.title",
                            )}
                        </h2>

                        <p className="
                            mt-1
                            text-left
                            text-sm
                            text-muted
                        ">
                            {t(
                                "rotateSecret.success.description",
                            )}
                        </p>
                    </div>

                    <button
                        type="button"
                        onClick={onClose}
                        className="
                            ml-4
                            shrink-0
                            rounded-md
                            p-2
                            text-muted
                            transition
                            hover:bg-secondary
                            hover:text-secondary-foreground
                        "
                        aria-label={t(
                            "rotateSecret.close",
                        )}
                    >
                        <X className="h-5 w-5" />
                    </button>
                </div>

                {/* Success content */}
                <div className="
                    w-full
                    min-w-0
                    p-6
                    text-left
                ">
                    {/* Success message */}
                    <div className="
                        w-full
                        rounded-md
                        border
                        border-success/20
                        bg-success/10
                        px-4
                        py-3
                        text-left
                    ">
                        <div className="
                            flex
                            items-start
                            gap-3
                            text-left
                        ">
                            <div className="
                                mt-0.5
                                flex
                                h-8
                                w-8
                                shrink-0
                                items-center
                                justify-center
                                rounded-full
                                bg-success/10
                                text-success
                            ">
                                <Check className="h-4 w-4" />
                            </div>

                            <div className="
                                min-w-0
                                flex-1
                                text-left
                            ">
                                <p className="
                                    text-left
                                    text-sm
                                    font-medium
                                    text-foreground
                                ">
                                    {t(
                                        "rotateSecret.success.rotated",
                                    )}
                                </p>

                                <p className="
                                    mt-1
                                    text-left
                                    text-sm
                                    text-muted
                                ">
                                    {t(
                                        "rotateSecret.success.secretWarning",
                                    )}
                                </p>
                            </div>
                        </div>
                    </div>

                    {/* Secret */}
                    <div className="
                        mt-6
                        w-full
                        min-w-0
                        text-left
                    ">
                        <label className="
                            mb-2
                            block
                            text-left
                            text-sm
                            font-medium
                            text-foreground
                        ">
                            {t(
                                "rotateSecret.secret",
                            )}
                        </label>

                        <div className="
                            flex
                            w-full
                            min-w-0
                            items-start
                            gap-2
                        ">
                            <div className="
                                min-w-0
                                flex-1
                                overflow-hidden
                                rounded-md
                                border
                                border-border
                                bg-background
                                px-3
                                py-2.5
                                text-left
                            ">
                                <code className="
                                    block
                                    w-full
                                    whitespace-normal
                                    break-all
                                    font-mono
                                    text-left
                                    text-sm
                                    leading-6
                                    text-foreground
                                ">
                                    {secret}
                                </code>
                            </div>

                            <Button
                                type="button"
                                variant="outline"
                                className="shrink-0"
                                onClick={handleCopy}
                            >
                                {isCopied ? (
                                    <>
                                        <Check className="
                                            mr-2
                                            h-4
                                            w-4
                                        " />

                                        {t(
                                            "rotateSecret.copied",
                                        )}
                                    </>
                                ) : (
                                    <>
                                        <Copy className="
                                            mr-2
                                            h-4
                                            w-4
                                        " />

                                        {t(
                                            "rotateSecret.copy",
                                        )}
                                    </>
                                )}
                            </Button>
                        </div>
                    </div>

                    {/* Warning */}
                    <div className="
                        mt-6
                        w-full
                        min-w-0
                        overflow-hidden
                        rounded-md
                        border
                        border-warning/20
                        bg-warning/10
                        px-4
                        py-3
                        text-left
                    ">
                        <p className="
                            w-full
                            min-w-0
                            whitespace-normal
                            break-words
                            text-left
                            text-sm
                            leading-6
                            text-foreground
                            [overflow-wrap:anywhere]
                        ">
                            {t(
                                "rotateSecret.success.saveWarning",
                            )}
                        </p>
                    </div>

                    {/* Footer */}
                    <div className="
                        mt-6
                        flex
                        w-full
                        justify-end
                    ">
                        <Button
                            type="button"
                            onClick={onClose}
                        >
                            {t(
                                "rotateSecret.done",
                            )}
                        </Button>
                    </div>
                </div>
            </div>
        </div>
    );
}