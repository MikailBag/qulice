package com.qulice.pmd;

import net.sourceforge.pmd.RuleViolation;
import net.sourceforge.pmd.Report.ConfigurationError;
import net.sourceforge.pmd.Report.ProcessingError;

/**
 * Base of all pmd-reported errors.
 * @since 1.0
 */
public interface PmdError {
    /**
     * Returns name which is short, fixed, human-readable category of the error.
     * @return Error name.
     */
    String name();

    /**
     * Returns file name which caused this error.
     * @return File name.
     */
    String fileName();

    /**
     * Returns formatted line range, related to this error.
     * May return some "empty" value if line information is not
     * available.
     * @return Formatted line range.
     */
    String lines();

    /**
     * Returns error description
     * @return Description.
     */
    String description();

    /**
     * PmdError backed by a RuleViolation.
     */
    final class OfRuleViolation implements PmdError {
        /**
         * Internal RuleViolation.
         */
        private final RuleViolation violation;

        /**
         * Creates a new PmdError, representing given RuleViolation.
         * @param violation Internal RuleViolation.
         */
        public OfRuleViolation(final RuleViolation violation) {
            this.violation = violation;
        }

        @Override
        public String name() {
            return this.violation.getRule().getName();
        }

        @Override
        public String fileName() {
            return this.violation.getFilename();
        }

        @Override
        public String lines() {
            return String.format(
                "%d-%d",
                this.violation.getBeginLine(), this.violation.getEndLine()
            );
        }

        @Override
        public String description() {
            return this.violation.getDescription();
        }
    }

    /**
     * PmdError backed by a ProcessingError.
     */
    final class OfProcessingError implements PmdError {
        /**
         * Internal ProcessingError.
         */
        private final ProcessingError error;

        /**
         * Creates a new PmdError, representing given ProcessingError.
         * @param error Internal ProcessingError.
         */
        public OfProcessingError(final ProcessingError error) {
            this.error = error;
        }

        @Override
        public String name() {
            return "ProcessingError";
        }

        @Override
        public String fileName() {
            return this.error.getFile();
        }

        @Override
        public String lines() {
            return "unknown";
        }

        @Override
        public String description() {
            return this.error.getMsg() + this.error.getDetail();
        }
    }

     /**
     * PmdError backed by a ConfigError.
     */
    final class OfConfigError implements PmdError {
        /**
         * Internal ConfigError.
         */
        private final ConfigurationError error;

        /**
         * Creates a new PmdError, representing given ProcessingError.
         * @param error Internal ProcessingError.
         */
        public OfConfigError(final ConfigurationError error) {
            this.error = error;
        }

        @Override
        public String name() {
            return "ProcessingError";
        }

        @Override
        public String fileName() {
            return "unknown";
        }

        @Override
        public String lines() {
            return "unknown";
        }

        @Override
        public String description() {
            return this.error.issue();
        }
    }
}
